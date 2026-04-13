package com.kiacms.ai.dto.response;

import com.kiacms.course.enums.CourseStatus;
import java.time.LocalDate;
import java.util.UUID;

public record AiChatbotCourseRecommendationResponse(
        UUID courseId,
        String courseCode,
        String courseTitle,
        String trackName,
        CourseStatus status,
        LocalDate startDate,
        LocalDate endDate,
        String reason
) {
}
