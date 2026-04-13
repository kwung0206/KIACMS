package com.kiacms.ai.dto.response;

import java.util.List;

public record CareerCourseRecommendationResponse(
        String careerGoal,
        List<CourseRecommendationItemResponse> recommendations
) {
}
