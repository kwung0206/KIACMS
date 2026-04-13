package com.kiacms.ai.service;

import com.kiacms.ai.dto.request.CareerCourseRecommendationRequest;
import com.kiacms.ai.dto.response.CareerCourseRecommendationResponse;
import com.kiacms.ai.dto.response.CourseRecommendationItemResponse;
import com.kiacms.ai.enums.AiFeatureType;
import com.kiacms.ai.enums.AiReferenceType;
import com.kiacms.course.entity.Course;
import com.kiacms.course.enums.CourseStatus;
import com.kiacms.course.repository.CourseRepository;
import com.kiacms.global.exception.BusinessException;
import com.kiacms.global.exception.ErrorCode;
import com.kiacms.user.entity.User;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CareerCourseRecommendationAiService {

    private final CourseRepository courseRepository;
    private final OpenAiStructuredResponseClient openAiStructuredResponseClient;
    private final AiPromptFactory aiPromptFactory;
    private final AiSchemaFactory aiSchemaFactory;

    @Transactional(readOnly = true)
    public CareerCourseRecommendationResponse recommendCourses(
            CareerCourseRecommendationRequest request,
            User requester
    ) {
        int limit = request.limit() == null ? 3 : request.limit();
        List<Course> candidateCourses = courseRepository.findAll(Sort.by(Sort.Direction.ASC, "startDate")).stream()
                .filter(course -> course.getStatus() != CourseStatus.ARCHIVED)
                .filter(AiCatalogFilter::isUsableCourse)
                .limit(25)
                .toList();

        if (candidateCourses.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "No course data is available for AI recommendation.");
        }

        CareerCourseRecommendationResponse rawResponse = openAiStructuredResponseClient.requestStructuredOutput(
                requester,
                AiFeatureType.CAREER_COURSE_RECOMMENDATION,
                AiReferenceType.FREE_TEXT,
                null,
                aiPromptFactory.buildCareerCourseRecommendationSystemPrompt(),
                aiPromptFactory.buildCareerCourseRecommendationUserPrompt(request, candidateCourses),
                "career_course_recommendation_response",
                aiSchemaFactory.careerCourseRecommendationSchema(),
                CareerCourseRecommendationResponse.class,
                1000
        );

        Map<UUID, Course> allowedCourses = candidateCourses.stream()
                .collect(java.util.stream.Collectors.toMap(Course::getId, Function.identity()));

        List<CourseRecommendationItemResponse> normalized = rawResponse.recommendations().stream()
                .filter(item -> item.courseId() != null && allowedCourses.containsKey(item.courseId()))
                .limit(limit)
                .sorted(Comparator.comparing(item -> item.recommendedRank() == null ? Integer.MAX_VALUE : item.recommendedRank()))
                .map(item -> new CourseRecommendationItemResponse(
                        item.courseId(),
                        allowedCourses.get(item.courseId()).getCourseCode(),
                        allowedCourses.get(item.courseId()).getTitle(),
                        allowedCourses.get(item.courseId()).getTrackName(),
                        0,
                        item.reason()
                ))
                .toList();

        List<CourseRecommendationItemResponse> ranked = java.util.stream.IntStream.range(0, normalized.size())
                .mapToObj(index -> new CourseRecommendationItemResponse(
                        normalized.get(index).courseId(),
                        normalized.get(index).courseCode(),
                        normalized.get(index).courseTitle(),
                        normalized.get(index).trackName(),
                        index + 1,
                        normalized.get(index).reason()
                ))
                .toList();

        return new CareerCourseRecommendationResponse(request.careerGoal(), ranked);
    }
}
