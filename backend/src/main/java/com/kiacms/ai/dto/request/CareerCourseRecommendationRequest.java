package com.kiacms.ai.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CareerCourseRecommendationRequest(
        @NotBlank
        @Size(max = 1000)
        String careerGoal,

        @Min(1)
        @Max(5)
        Integer limit
) {
}
