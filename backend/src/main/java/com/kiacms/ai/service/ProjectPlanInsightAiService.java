package com.kiacms.ai.service;

import com.kiacms.ai.dto.request.ProjectPlanInsightRequest;
import com.kiacms.ai.dto.response.AiChatbotCourseRecommendationResponse;
import com.kiacms.ai.dto.response.AiChatbotProjectRecommendationResponse;
import com.kiacms.ai.dto.response.ProjectPlanInsightResponse;
import com.kiacms.ai.dto.response.ProjectPlanInsightStructuredResponse;
import com.kiacms.ai.dto.response.ProjectPlanMatchedStudentResponse;
import com.kiacms.ai.enums.AiFeatureType;
import com.kiacms.ai.enums.AiReferenceType;
import com.kiacms.course.entity.Course;
import com.kiacms.course.entity.Enrollment;
import com.kiacms.course.enums.CourseStatus;
import com.kiacms.course.enums.EnrollmentStatus;
import com.kiacms.course.repository.CourseRepository;
import com.kiacms.course.repository.EnrollmentRepository;
import com.kiacms.global.exception.AccessDeniedBusinessException;
import com.kiacms.global.exception.BusinessException;
import com.kiacms.global.exception.ErrorCode;
import com.kiacms.notification.service.NotificationService;
import com.kiacms.project.entity.ProjectPost;
import com.kiacms.project.enums.ProjectPostStatus;
import com.kiacms.project.repository.ProjectApplicationRepository;
import com.kiacms.project.repository.ProjectPositionRepository;
import com.kiacms.project.repository.ProjectPostRepository;
import com.kiacms.project.service.ProjectPostService;
import com.kiacms.user.entity.User;
import com.kiacms.user.enums.RoleType;
import com.kiacms.user.enums.UserStatus;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectPlanInsightAiService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ProjectPostRepository projectPostRepository;
    private final ProjectPositionRepository projectPositionRepository;
    private final ProjectApplicationRepository projectApplicationRepository;
    private final ProjectPostService projectPostService;
    private final NotificationService notificationService;
    private final OpenAiStructuredResponseClient openAiStructuredResponseClient;
    private final AiPromptFactory aiPromptFactory;
    private final AiSchemaFactory aiSchemaFactory;

    @Transactional
    public ProjectPlanInsightResponse analyze(ProjectPlanInsightRequest request, User requester) {
        ProjectPost projectPost = request.projectPostId() == null ? null : projectPostService.getProjectPost(request.projectPostId());
        validateProjectAccess(projectPost, requester, request.sendNotifications());

        String resolvedProjectTitle = projectPost == null ? "자유 입력 프로젝트" : projectPost.getTitle();
        String resolvedProjectPlanText = resolveProjectPlanText(projectPost, request.projectPlanText());
        if (resolvedProjectPlanText.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "프로젝트 설명 또는 계획서 내용을 입력해 주세요.");
        }

        List<Course> candidateCourses = courseRepository.findAll(Sort.by(Sort.Direction.ASC, "startDate")).stream()
                .filter(course -> course.getStatus() != CourseStatus.ARCHIVED)
                .filter(AiCatalogFilter::isUsableCourse)
                .limit(20)
                .toList();

        List<ProjectPost> candidatePosts = projectPostRepository.findAllVisibleOrderByCreatedAtDesc().stream()
                .filter(post -> projectPost == null || !post.getId().equals(projectPost.getId()))
                .limit(12)
                .toList();

        List<AiPromptFactory.ProjectContext> projectContexts = candidatePosts.stream()
                .map(post -> new AiPromptFactory.ProjectContext(
                        post,
                        projectPositionRepository.findAllByProjectPostOrderByCreatedAtAsc(post)
                ))
                .toList();

        ProjectPlanInsightStructuredResponse rawResponse = openAiStructuredResponseClient.requestStructuredOutput(
                requester,
                AiFeatureType.PROJECT_PLAN_ANALYSIS,
                projectPost == null ? AiReferenceType.FREE_TEXT : AiReferenceType.PROJECT_POST,
                projectPost == null ? null : projectPost.getId(),
                aiPromptFactory.buildProjectPlanInsightSystemPrompt(),
                aiPromptFactory.buildProjectPlanInsightUserPrompt(
                        request,
                        resolvedProjectTitle,
                        resolvedProjectPlanText,
                        candidateCourses,
                        projectContexts
                ),
                "project_plan_insight_response",
                aiSchemaFactory.projectPlanInsightSchema(),
                ProjectPlanInsightStructuredResponse.class,
                1400
        );

        Map<UUID, Course> courseMap = candidateCourses.stream()
                .collect(Collectors.toMap(Course::getId, course -> course));
        Map<UUID, ProjectPost> projectMap = candidatePosts.stream()
                .collect(Collectors.toMap(ProjectPost::getId, post -> post));

        List<AiChatbotCourseRecommendationResponse> recommendedCourses = normalizeCourses(rawResponse, courseMap);
        List<AiChatbotProjectRecommendationResponse> similarProjects = normalizeProjects(rawResponse, projectMap);

        NotificationDispatchResult dispatchResult = request.sendNotifications() && projectPost != null
                ? dispatchNotifications(projectPost, recommendedCourses, rawResponse)
                : NotificationDispatchResult.empty();

        return new ProjectPlanInsightResponse(
                projectPost == null ? null : projectPost.getId(),
                resolvedProjectTitle,
                rawResponse.analysisSummary(),
                safeStringList(rawResponse.keywords()),
                recommendedCourses,
                similarProjects,
                rawResponse.notificationMessage(),
                dispatchResult.matchedStudents().size(),
                dispatchResult.notificationCount(),
                dispatchResult.matchedStudents()
        );
    }

    private void validateProjectAccess(ProjectPost projectPost, User requester, boolean sendNotifications) {
        if (projectPost == null) {
            if (sendNotifications) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST, "알림 전송은 저장된 프로젝트 모집글에서만 사용할 수 있습니다.");
            }
            return;
        }

        if (projectPost.getStatus() == ProjectPostStatus.DELETED) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "삭제된 프로젝트는 AI 분석을 진행할 수 없습니다.");
        }

        if (!sendNotifications) {
            return;
        }

        boolean isOwner = projectPost.getOwner().getId().equals(requester.getId());
        if (!isOwner && requester.getRoleType() != RoleType.ROOT) {
            throw new AccessDeniedBusinessException("프로젝트 작성자 또는 Root만 추천 알림을 전송할 수 있습니다.");
        }
    }

    private String resolveProjectPlanText(ProjectPost projectPost, String projectPlanText) {
        StringBuilder builder = new StringBuilder();
        if (projectPost != null) {
            appendLine(builder, "제목: " + projectPost.getTitle());
            appendLine(builder, "소개: " + safe(projectPost.getDescription()));
            appendLine(builder, "목표: " + safe(projectPost.getGoal()));
            appendLine(builder, "기술 스택: " + safe(projectPost.getTechStack()));
            appendLine(builder, "PM 소개: " + safe(projectPost.getPmIntroduction()));
            appendLine(builder, "PM 경험: " + safe(projectPost.getPmBackground()));
        }
        if (projectPlanText != null && !projectPlanText.isBlank()) {
            appendLine(builder, "추가 계획서 메모: " + projectPlanText.trim());
        }
        return builder.toString().trim();
    }

    private List<AiChatbotCourseRecommendationResponse> normalizeCourses(
            ProjectPlanInsightStructuredResponse rawResponse,
            Map<UUID, Course> courseMap
    ) {
        if (rawResponse.recommendedCourses() == null) {
            return List.of();
        }

        return rawResponse.recommendedCourses().stream()
                .filter(Objects::nonNull)
                .filter(item -> item.courseId() != null && courseMap.containsKey(item.courseId()))
                .limit(4)
                .map(item -> {
                    Course course = courseMap.get(item.courseId());
                    return new AiChatbotCourseRecommendationResponse(
                            course.getId(),
                            course.getCourseCode(),
                            course.getTitle(),
                            course.getTrackName(),
                            course.getStatus(),
                            course.getStartDate(),
                            course.getEndDate(),
                            item.reason()
                    );
                })
                .toList();
    }

    private List<AiChatbotProjectRecommendationResponse> normalizeProjects(
            ProjectPlanInsightStructuredResponse rawResponse,
            Map<UUID, ProjectPost> projectMap
    ) {
        if (rawResponse.similarProjects() == null) {
            return List.of();
        }

        return rawResponse.similarProjects().stream()
                .filter(Objects::nonNull)
                .filter(item -> item.projectPostId() != null && projectMap.containsKey(item.projectPostId()))
                .limit(4)
                .map(item -> {
                    ProjectPost post = projectMap.get(item.projectPostId());
                    return new AiChatbotProjectRecommendationResponse(
                            post.getId(),
                            post.getTitle(),
                            post.getOwner().getName(),
                            post.getStatus(),
                            post.getRecruitUntil(),
                            item.reason(),
                            item.recommendedPosition()
                    );
                })
                .toList();
    }

    private NotificationDispatchResult dispatchNotifications(
            ProjectPost projectPost,
            List<AiChatbotCourseRecommendationResponse> recommendedCourses,
            ProjectPlanInsightStructuredResponse rawResponse
    ) {
        if (recommendedCourses.isEmpty()) {
            return NotificationDispatchResult.empty();
        }

        List<Course> courses = recommendedCourses.stream()
                .map(AiChatbotCourseRecommendationResponse::courseId)
                .distinct()
                .map(courseId -> courseRepository.findById(courseId).orElse(null))
                .filter(Objects::nonNull)
                .toList();

        List<Enrollment> enrollments = enrollmentRepository.findAllByCourseInAndStatusOrderByCreatedAtAsc(
                courses,
                EnrollmentStatus.ENROLLED
        );

        Map<UUID, Course> courseLookup = courses.stream()
                .collect(Collectors.toMap(Course::getId, course -> course));

        Map<UUID, ProjectPlanMatchedStudentResponse> matchedStudents = new LinkedHashMap<>();
        Set<UUID> notifiedStudentIds = new LinkedHashSet<>();
        int notificationCount = 0;

        for (Enrollment enrollment : enrollments) {
            User student = enrollment.getStudent();
            if (student == null) {
                continue;
            }
            if (student.getRoleType() != RoleType.STUDENT || student.getStatus() != UserStatus.APPROVED) {
                continue;
            }
            if (student.getId().equals(projectPost.getOwner().getId())) {
                continue;
            }
            if (projectApplicationRepository.existsByProjectPosition_ProjectPostAndApplicant(projectPost, student)) {
                continue;
            }

            Course matchedCourse = courseLookup.get(enrollment.getCourse().getId());
            if (matchedCourse == null) {
                continue;
            }

            matchedStudents.putIfAbsent(
                    student.getId(),
                    new ProjectPlanMatchedStudentResponse(student.getId(), student.getName(), matchedCourse.getTitle())
            );

            if (notifiedStudentIds.add(student.getId())) {
                notificationService.createProjectAiRecommendedNotification(
                        student,
                        projectPost,
                        matchedCourse,
                        safeStringList(rawResponse.keywords()),
                        rawResponse.notificationMessage()
                );
                notificationCount++;
            }
        }

        return new NotificationDispatchResult(List.copyOf(matchedStudents.values()), notificationCount);
    }

    private List<String> safeStringList(List<String> values) {
        if (values == null) {
            return List.of();
        }

        Set<String> normalized = new LinkedHashSet<>();
        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }
            normalized.add(value.trim());
        }
        return List.copyOf(normalized);
    }

    private void appendLine(StringBuilder builder, String line) {
        if (line == null || line.isBlank()) {
            return;
        }
        if (!builder.isEmpty()) {
            builder.append(System.lineSeparator());
        }
        builder.append(line);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private record NotificationDispatchResult(
            List<ProjectPlanMatchedStudentResponse> matchedStudents,
            int notificationCount
    ) {
        private static NotificationDispatchResult empty() {
            return new NotificationDispatchResult(List.of(), 0);
        }
    }
}
