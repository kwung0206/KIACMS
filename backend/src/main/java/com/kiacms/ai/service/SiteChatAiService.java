package com.kiacms.ai.service;

import com.kiacms.ai.dto.request.AiChatbotMessageRequest;
import com.kiacms.ai.dto.request.CareerCourseRecommendationRequest;
import com.kiacms.ai.dto.request.SimilarProjectRecommendationRequest;
import com.kiacms.ai.dto.response.AiChatbotCourseRecommendationResponse;
import com.kiacms.ai.dto.response.AiChatbotLinkResponse;
import com.kiacms.ai.dto.response.AiChatbotProjectRecommendationResponse;
import com.kiacms.ai.dto.response.AiChatbotResponse;
import com.kiacms.ai.dto.response.AiChatbotStructuredResponse;
import com.kiacms.ai.dto.response.CareerCourseRecommendationResponse;
import com.kiacms.ai.dto.response.SimilarProjectRecommendationResponse;
import com.kiacms.ai.enums.AiFeatureType;
import com.kiacms.ai.enums.AiReferenceType;
import com.kiacms.ai.enums.ChatbotIntentType;
import com.kiacms.course.entity.Course;
import com.kiacms.course.enums.CourseStatus;
import com.kiacms.course.repository.CourseRepository;
import com.kiacms.project.entity.ProjectPost;
import com.kiacms.project.enums.ProjectPostStatus;
import com.kiacms.project.repository.ProjectPositionRepository;
import com.kiacms.project.repository.ProjectPostRepository;
import com.kiacms.user.entity.User;
import com.kiacms.user.enums.RoleType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
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
public class SiteChatAiService {

    private final CourseRepository courseRepository;
    private final ProjectPostRepository projectPostRepository;
    private final ProjectPositionRepository projectPositionRepository;
    private final OpenAiStructuredResponseClient openAiStructuredResponseClient;
    private final AiPromptFactory aiPromptFactory;
    private final AiSchemaFactory aiSchemaFactory;
    private final CareerCourseRecommendationAiService careerCourseRecommendationAiService;
    private final SimilarProjectRecommendationAiService similarProjectRecommendationAiService;

    @Transactional(readOnly = true)
    public AiChatbotResponse chat(AiChatbotMessageRequest request, User requester) {
        List<Course> candidateCourses = courseRepository.findAll(Sort.by(Sort.Direction.ASC, "startDate")).stream()
                .filter(course -> course.getStatus() != CourseStatus.ARCHIVED)
                .filter(AiCatalogFilter::isUsableCourse)
                .limit(20)
                .toList();

        List<ProjectPost> candidatePosts = projectPostRepository.findAllVisibleOrderByCreatedAtDesc().stream()
                .filter(post -> post.getStatus() != ProjectPostStatus.ARCHIVED)
                .limit(12)
                .toList();

        List<AiPromptFactory.ProjectContext> projectContexts = candidatePosts.stream()
                .map(post -> new AiPromptFactory.ProjectContext(
                        post,
                        projectPositionRepository.findAllByProjectPostOrderByCreatedAtAsc(post)
                ))
                .toList();

        List<AiPromptFactory.SiteRouteContext> routeCatalog = buildRouteCatalog(requester.getRoleType());
        Map<String, AiPromptFactory.SiteRouteContext> routeMap = routeCatalog.stream()
                .collect(Collectors.toMap(AiPromptFactory.SiteRouteContext::routeKey, route -> route, (left, right) -> left, LinkedHashMap::new));

        AiChatbotResponse guidedResponse = tryBuildGuidedSiteResponse(request.message(), requester.getRoleType(), routeMap);
        if (guidedResponse != null) {
            return guidedResponse;
        }

        AiChatbotResponse careerResponse = tryBuildCareerRecommendationResponse(request.message(), requester, routeMap);
        if (careerResponse != null) {
            return careerResponse;
        }

        AiChatbotResponse projectRecommendationResponse = tryBuildProjectRecommendationResponse(request.message(), requester, routeMap);
        if (projectRecommendationResponse != null) {
            return projectRecommendationResponse;
        }

        AiChatbotStructuredResponse rawResponse = openAiStructuredResponseClient.requestStructuredOutput(
                requester,
                AiFeatureType.SITE_CHAT_ASSISTANT,
                AiReferenceType.FREE_TEXT,
                null,
                aiPromptFactory.buildSiteChatbotSystemPrompt(),
                aiPromptFactory.buildSiteChatbotUserPrompt(request, requester, routeCatalog, candidateCourses, projectContexts),
                "site_chatbot_response",
                aiSchemaFactory.siteChatbotSchema(),
                AiChatbotStructuredResponse.class,
                1200
        );

        Map<UUID, Course> allowedCourses = candidateCourses.stream()
                .collect(Collectors.toMap(Course::getId, course -> course));
        Map<UUID, ProjectPost> allowedProjects = candidatePosts.stream()
                .collect(Collectors.toMap(ProjectPost::getId, post -> post));
        return new AiChatbotResponse(
                rawResponse.intentType(),
                rawResponse.answer(),
                safeStringList(rawResponse.keywords(), 6),
                normalizeLinks(rawResponse, routeMap, request.message(), requester.getRoleType()),
                safeStringList(rawResponse.followUpQuestions(), 4),
                normalizeCourses(rawResponse, allowedCourses),
                normalizeProjects(rawResponse, allowedProjects)
        );
    }

    private AiChatbotResponse tryBuildGuidedSiteResponse(
            String question,
            RoleType roleType,
            Map<String, AiPromptFactory.SiteRouteContext> routeMap
    ) {
        if (question == null || question.isBlank()) {
            return null;
        }

        String normalized = normalizeQuestion(question);

        if (roleType == RoleType.STUDENT
                && containsAny(normalized, "지원서", "지원서수정", "내지원현황")
                && containsAny(normalized, "어디", "수정", "고치", "변경")) {
            return guidedResponse(
                    "학생 지원서는 프로젝트 상세 화면에서 처음 작성하고, 제출 후에는 내 지원 현황에서 다시 열어 수정·삭제·철회할 수 있습니다. 먼저 프로젝트 게시판에서 모집글을 열고 지원서를 제출한 뒤, 이후 관리가 필요할 때는 내 지원 현황으로 이동하면 가장 편합니다.",
                    List.of("지원서", "프로젝트", "수정"),
                    routeMap,
                    List.of("PROJECT_BOARD", "STUDENT_APPLICATIONS"),
                    List.of("내 모집글은 어디서 보나요?", "프로젝트 계획서를 보고 관련 강좌를 추천해줄 수 있나요?")
            );
        }

        if (roleType == RoleType.STUDENT && containsAny(normalized, "내모집글", "내가쓴모집글", "모집글관리")) {
            return guidedResponse(
                    "내가 작성한 모집글은 내 모집글 화면에서 확인할 수 있습니다. 새 모집글 작성 화면에서 등록한 뒤에는 내 모집글에서 상세 이동과 지원서 관리까지 이어서 처리하면 됩니다.",
                    List.of("모집글", "학생 PM"),
                    routeMap,
                    List.of("STUDENT_PROJECTS_ME", "STUDENT_PROJECTS_NEW"),
                    List.of("지원서는 어디서 수정하나요?", "프로젝트 게시판은 어디서 보나요?")
            );
        }

        if (roleType == RoleType.STUDENT && containsAny(normalized, "프로젝트계획서", "계획서분석", "프로젝트분석")) {
            return guidedResponse(
                    "프로젝트 계획서 기반 추천은 두 가지 방식으로 사용할 수 있습니다. 챗봇에 계획서 내용을 그대로 붙여 넣어 질문하면 관련 강좌와 유사 프로젝트를 추천해 드리고, 내가 만든 모집글 상세에서는 AI 프로젝트 분석 패널에서 추천 알림까지 보낼 수 있습니다.",
                    List.of("프로젝트 계획서", "AI 분석", "추천 알림"),
                    routeMap,
                    List.of("PROJECT_BOARD", "STUDENT_PROJECTS_ME"),
                    List.of("내 모집글은 어디서 보나요?", "저는 백엔드 개발자가 되고 싶은데 어떤 강좌를 들으면 좋을까요?")
            );
        }

        if (containsAny(normalized, "승인", "반려") && containsAny(normalized, "어디", "화면", "페이지")) {
            if (roleType == RoleType.ROOT) {
                return guidedResponse(
                        "회원가입 승인과 반려 처리는 Root 운영 홈에서 합니다. 승인 대기 목록을 검토한 뒤 바로 승인하거나, 반려 사유를 입력해 처리할 수 있습니다.",
                        List.of("승인", "반려", "Root"),
                        routeMap,
                        List.of("ROOT_HOME"),
                        List.of("수업 일정 관리는 어디서 하나요?", "프로젝트 삭제 관리는 어디서 하나요?")
                );
            }

            return guidedResponse(
                    "회원가입 승인과 반려는 Root 권한에서만 처리할 수 있습니다. 현재 계정이 Root가 아니라면 Root 운영 홈에서 담당자가 처리해야 합니다.",
                    List.of("승인", "Root"),
                    routeMap,
                    List.of("ROOT_HOME"),
                    List.of("멘토는 어떤 기능을 사용할 수 있나요?", "내 모집글은 어디서 볼 수 있나요?")
            );
        }

        if (containsAny(normalized, "멘토기능", "멘토는", "멘토역할")) {
            return guidedResponse(
                    "KIACMS의 멘토는 프로젝트 참가자가 아니라 수강생을 관리하는 관리자 역할입니다. 관리 학생 화면에서 담당 학생을 배정하고, 학생에게 과정을 매핑해 캘린더에 수업이 반영되도록 관리합니다.",
                    List.of("멘토", "관리 학생", "과정 매핑"),
                    routeMap,
                    List.of("MENTOR_STUDENTS"),
                    List.of("학생에게 수업을 어떻게 매핑하나요?", "관리 학생은 어디서 배정하나요?")
            );
        }

        if (roleType == RoleType.INSTRUCTOR
                && (containsAny(normalized, "태그된정리글", "정리글태그", "태그노트")
                || containsAny(normalized, "코멘트", "댓글"))) {
            return guidedResponse(
                    "강사는 태그된 정리글 화면에서 자신이 태그된 학생 노트를 확인하고 코멘트를 남길 수 있습니다. 학생이 강사를 태그하면 알림에서도 바로 이동할 수 있습니다.",
                    List.of("강사", "정리글", "코멘트"),
                    routeMap,
                    List.of("INSTRUCTOR_TAGGED_NOTES", "NOTIFICATIONS"),
                    List.of("담당 회차는 어디서 관리하나요?", "프로젝트 지원 이력은 어디서 보나요?")
            );
        }

        if (roleType == RoleType.INSTRUCTOR
                && (containsAny(normalized, "담당회차", "회차관리") || containsAny(normalized, "zoom", "녹화본", "요약링크"))) {
            return guidedResponse(
                    "강사의 회차별 Zoom 링크, 녹화본 링크, 정리 링크 관리는 담당 회차 관리 화면에서 할 수 있습니다. 각 회차별로 리소스를 저장하면 해당 학생들의 캘린더와 상세 화면에 바로 반영됩니다.",
                    List.of("강사", "회차", "Zoom"),
                    routeMap,
                    List.of("INSTRUCTOR_SESSIONS"),
                    List.of("태그된 정리글은 어디서 확인하나요?", "프로젝트 지원 이력은 어디서 보나요?")
            );
        }

        return null;
    }

    private AiChatbotResponse tryBuildCareerRecommendationResponse(
            String question,
            User requester,
            Map<String, AiPromptFactory.SiteRouteContext> routeMap
    ) {
        if (!isCareerQuestion(question)) {
            return null;
        }

        CareerCourseRecommendationResponse response = careerCourseRecommendationAiService.recommendCourses(
                new CareerCourseRecommendationRequest(question.trim(), 3),
                requester
        );

        List<AiChatbotCourseRecommendationResponse> recommendedCourses = response.recommendations().stream()
                .map(item -> new AiChatbotCourseRecommendationResponse(
                        item.courseId(),
                        item.courseCode(),
                        item.courseTitle(),
                        item.trackName(),
                        null,
                        null,
                        null,
                        item.reason()
                ))
                .toList();

        String answer = recommendedCourses.isEmpty()
                ? "현재 등록된 강좌 기준으로 바로 추천할 수 있는 과정이 많지 않습니다. 조금 더 구체적인 진로 방향이나 관심 기술을 알려 주시면 다시 추천해 드릴게요."
                : "현재 등록된 강좌 기준으로는 아래 과정들을 우선 추천드립니다. 진로 목표와 가장 맞는 순서대로 골랐고, 이유도 함께 정리해 두었습니다.";

        return new AiChatbotResponse(
                ChatbotIntentType.COURSE_RECOMMENDATION,
                answer,
                List.of("진로", "강좌 추천"),
                buildRouteLinks(routeMap, List.of("STUDENT_CALENDAR", "PROJECT_BOARD")),
                List.of("프로젝트 계획서를 보고 관련 강좌를 추천해줄 수 있나요?", "유사한 프로젝트도 추천해줄 수 있나요?"),
                recommendedCourses,
                List.of()
        );
    }

    private AiChatbotResponse tryBuildProjectRecommendationResponse(
            String question,
            User requester,
            Map<String, AiPromptFactory.SiteRouteContext> routeMap
    ) {
        if (!isProjectRecommendationQuestion(question)) {
            return null;
        }

        SimilarProjectRecommendationResponse response = similarProjectRecommendationAiService.recommendProjects(
                new SimilarProjectRecommendationRequest(question.trim(), 3),
                requester
        );

        List<AiChatbotProjectRecommendationResponse> recommendedProjects = response.recommendations().stream()
                .map(item -> {
                    ProjectPost post = projectPostRepository.findById(item.projectPostId()).orElse(null);
                    return new AiChatbotProjectRecommendationResponse(
                            item.projectPostId(),
                            item.title(),
                            post == null ? "-" : post.getOwner().getName(),
                            post == null ? null : post.getStatus(),
                            post == null ? null : post.getRecruitUntil(),
                            item.similarityReason(),
                            item.recommendedPosition()
                    );
                })
                .toList();

        return new AiChatbotResponse(
                ChatbotIntentType.PROJECT_RECOMMENDATION,
                recommendedProjects.isEmpty()
                        ? "현재 프로젝트 게시판 기준으로 바로 연결할 수 있는 유사 프로젝트가 많지 않습니다. 아이디어를 조금 더 구체적으로 적어 주시면 다시 비교해 드릴게요."
                        : "현재 프로젝트 게시판 기준으로 유사도가 높은 모집글을 정리했습니다. 상세로 들어가면 포지션과 모집 상태를 바로 확인할 수 있습니다.",
                List.of("프로젝트", "유사도"),
                buildRouteLinks(routeMap, List.of("PROJECT_BOARD")),
                List.of("내 모집글은 어디서 보나요?", "프로젝트 계획서를 보고 관련 강좌를 추천해줄 수 있나요?"),
                List.of(),
                recommendedProjects
        );
    }

    private AiChatbotResponse guidedResponse(
            String answer,
            List<String> keywords,
            Map<String, AiPromptFactory.SiteRouteContext> routeMap,
            List<String> routeKeys,
            List<String> followUpQuestions
    ) {
        return new AiChatbotResponse(
                ChatbotIntentType.SITE_GUIDE,
                answer,
                keywords,
                buildRouteLinks(routeMap, routeKeys),
                followUpQuestions,
                List.of(),
                List.of()
        );
    }

    private List<AiChatbotLinkResponse> buildRouteLinks(
            Map<String, AiPromptFactory.SiteRouteContext> routeMap,
            List<String> routeKeys
    ) {
        return routeKeys.stream()
                .map(routeMap::get)
                .filter(Objects::nonNull)
                .map(route -> new AiChatbotLinkResponse(route.label(), route.url()))
                .toList();
    }

    private List<AiChatbotCourseRecommendationResponse> normalizeCourses(
            AiChatbotStructuredResponse rawResponse,
            Map<UUID, Course> allowedCourses
    ) {
        if (rawResponse.recommendedCourses() == null) {
            return List.of();
        }

        return rawResponse.recommendedCourses().stream()
                .filter(Objects::nonNull)
                .filter(item -> item.courseId() != null && allowedCourses.containsKey(item.courseId()))
                .sorted(Comparator.comparing(item -> allowedCourses.get(item.courseId()).getStartDate()))
                .limit(3)
                .map(item -> {
                    Course course = allowedCourses.get(item.courseId());
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
            AiChatbotStructuredResponse rawResponse,
            Map<UUID, ProjectPost> allowedProjects
    ) {
        if (rawResponse.recommendedProjects() == null) {
            return List.of();
        }

        return rawResponse.recommendedProjects().stream()
                .filter(Objects::nonNull)
                .filter(item -> item.projectPostId() != null && allowedProjects.containsKey(item.projectPostId()))
                .limit(3)
                .map(item -> {
                    ProjectPost post = allowedProjects.get(item.projectPostId());
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

    private List<AiChatbotLinkResponse> normalizeLinks(
            AiChatbotStructuredResponse rawResponse,
            Map<String, AiPromptFactory.SiteRouteContext> routeMap,
            String question,
            RoleType roleType
    ) {
        LinkedHashSet<String> routeKeys = new LinkedHashSet<>();

        if (rawResponse.suggestedRouteKeys() != null) {
            rawResponse.suggestedRouteKeys().stream()
                    .filter(routeMap::containsKey)
                    .forEach(routeKeys::add);
        }

        inferFallbackRouteKeys(question, roleType).stream()
                .filter(routeMap::containsKey)
                .forEach(routeKeys::add);

        return routeKeys.stream()
                .limit(4)
                .map(routeMap::get)
                .filter(Objects::nonNull)
                .map(route -> new AiChatbotLinkResponse(route.label(), route.url()))
                .toList();
    }

    private List<String> inferFallbackRouteKeys(String question, RoleType roleType) {
        if (question == null || question.isBlank()) {
            return List.of();
        }

        String lowered = normalizeQuestion(question);
        List<String> keys = new ArrayList<>();

        if (containsAny(lowered, "지원서", "지원현황")) {
            if (roleType == RoleType.STUDENT) {
                keys.add("STUDENT_APPLICATIONS");
            }
            if (roleType == RoleType.ROOT) {
                keys.add("ROOT_HOME");
            }
        }
        if (containsAny(lowered, "내모집글", "모집글관리")) {
            keys.add("STUDENT_PROJECTS_ME");
        }
        if (containsAny(lowered, "프로젝트", "모집글")) {
            keys.add("PROJECT_BOARD");
        }
        if (containsAny(lowered, "캘린더", "수업", "회차")) {
            keys.add(roleType == RoleType.ROOT ? "ROOT_COURSES" : "STUDENT_CALENDAR");
        }
        if (containsAny(lowered, "승인", "반려")) {
            keys.add("ROOT_HOME");
        }
        if (containsAny(lowered, "정리글", "노트")) {
            keys.add(roleType == RoleType.INSTRUCTOR ? "INSTRUCTOR_TAGGED_NOTES" : "STUDENT_NOTES");
        }
        if (containsAny(lowered, "멘토")) {
            keys.add("MENTOR_STUDENTS");
        }
        if (containsAny(lowered, "마이페이지", "회원정보")) {
            keys.add("MY_PAGE");
        }
        if (containsAny(lowered, "알림") && roleType != RoleType.MENTOR) {
            keys.add("NOTIFICATIONS");
        }

        return keys;
    }

    private List<AiPromptFactory.SiteRouteContext> buildRouteCatalog(RoleType roleType) {
        List<AiPromptFactory.SiteRouteContext> routes = new ArrayList<>(List.of(
                new AiPromptFactory.SiteRouteContext("MY_PAGE", "마이페이지", "/me", "내 정보 조회와 회원정보 수정, 비밀번호 변경"),
                new AiPromptFactory.SiteRouteContext("PROJECT_BOARD", "프로젝트 게시판", "/projects", "프로젝트 모집글 탐색과 상세 확인")
        ));

        if (roleType != RoleType.MENTOR) {
            routes.add(new AiPromptFactory.SiteRouteContext("NOTIFICATIONS", "알림", "/notifications", "알림 목록, 읽음 처리, 삭제"));
        }

        switch (roleType) {
            case STUDENT -> routes.addAll(List.of(
                    new AiPromptFactory.SiteRouteContext("STUDENT_HOME", "학생 홈", "/student", "학생용 홈 화면"),
                    new AiPromptFactory.SiteRouteContext("STUDENT_CALENDAR", "학생 수업 캘린더", "/student/calendar", "수업 일정, Zoom 링크, 녹화본 링크 확인"),
                    new AiPromptFactory.SiteRouteContext("STUDENT_NOTES", "내 정리글", "/student/notes", "정리글 목록, 작성, 수정, 상세 확인"),
                    new AiPromptFactory.SiteRouteContext("STUDENT_NOTE_NEW", "정리글 작성", "/student/notes/new", "새 정리글 작성"),
                    new AiPromptFactory.SiteRouteContext("STUDENT_PROJECTS_ME", "내 모집글", "/student/projects/me", "내가 작성한 프로젝트 모집글 관리"),
                    new AiPromptFactory.SiteRouteContext("STUDENT_PROJECTS_NEW", "새 모집글 작성", "/student/projects/new", "새 프로젝트 모집글 작성"),
                    new AiPromptFactory.SiteRouteContext("STUDENT_APPLICATIONS", "내 지원 현황", "/student/applications", "프로젝트 지원서 조회, 수정, 삭제, 철회")
            ));
            case INSTRUCTOR -> routes.addAll(List.of(
                    new AiPromptFactory.SiteRouteContext("INSTRUCTOR_HOME", "강사 홈", "/instructor", "강사용 홈 화면"),
                    new AiPromptFactory.SiteRouteContext("INSTRUCTOR_SESSIONS", "담당 회차 관리", "/instructor/sessions", "Zoom, 녹화본, 정리 링크 등록"),
                    new AiPromptFactory.SiteRouteContext("INSTRUCTOR_TAGGED_NOTES", "태그된 정리글", "/instructor/tagged-notes", "학생이 태그한 정리글 확인과 코멘트 작성"),
                    new AiPromptFactory.SiteRouteContext("INSTRUCTOR_PROJECT_SUPPORT", "프로젝트 지원 이력", "/instructor/project-mentoring", "강사의 프로젝트 지원 이력 확인")
            ));
            case MENTOR -> routes.addAll(List.of(
                    new AiPromptFactory.SiteRouteContext("MENTOR_HOME", "멘토 홈", "/mentor", "멘토용 홈 화면"),
                    new AiPromptFactory.SiteRouteContext("MENTOR_STUDENTS", "관리 학생", "/mentor/students", "담당 학생 관리와 학생-수업 매핑")
            ));
            case ROOT -> routes.addAll(List.of(
                    new AiPromptFactory.SiteRouteContext("ROOT_HOME", "Root 운영 홈", "/root", "회원가입 승인과 운영 요약"),
                    new AiPromptFactory.SiteRouteContext("ROOT_COURSES", "수업 일정 관리", "/root/courses", "캘린더 기반 수업 일정 등록과 수정"),
                    new AiPromptFactory.SiteRouteContext("ROOT_PROJECTS", "프로젝트 삭제 관리", "/root/projects", "프로젝트 삭제와 삭제 이력 확인")
            ));
        }

        return routes;
    }

    private List<String> safeStringList(List<String> values, int maxSize) {
        if (values == null) {
            return List.of();
        }

        Set<String> normalized = new LinkedHashSet<>();
        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }
            normalized.add(value.trim());
            if (normalized.size() >= maxSize) {
                break;
            }
        }
        return List.copyOf(normalized);
    }

    private boolean isCareerQuestion(String question) {
        if (question == null || question.isBlank()) {
            return false;
        }

        String lowered = normalizeQuestion(question);
        if (containsAny(lowered, "프로젝트계획서", "계획서분석")) {
            return false;
        }

        return containsAny(
                lowered,
                "되고싶",
                "진로",
                "어떤강좌",
                "어떤수업",
                "추천강좌",
                "추천수업",
                "백엔드개발자",
                "정보보안",
                "보안쪽",
                "ai엔지니어"
        );
    }

    private boolean isProjectRecommendationQuestion(String question) {
        if (question == null || question.isBlank()) {
            return false;
        }

        String lowered = normalizeQuestion(question);
        if (containsAny(lowered, "어디", "수정", "승인")) {
            return false;
        }

        return containsAny(lowered, "유사프로젝트", "비슷한프로젝트")
                || (containsAny(lowered, "프로젝트") && containsAny(lowered, "추천"));
    }

    private String normalizeQuestion(String question) {
        return question == null
                ? ""
                : question.toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", "")
                .replace("?", "")
                .replace("!", "")
                .replace(".", "")
                .replace(",", "");
    }

    private boolean containsAny(String source, String... keywords) {
        for (String keyword : keywords) {
            if (source.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
