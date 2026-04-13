package com.kiacms.ai.dto.response;

import java.util.UUID;

public record CourseRecommendationItemResponse(
        UUID courseId,
        String courseCode,
        String courseTitle,
        String trackName,
        Integer recommendedRank,
        String reason
) {
}
