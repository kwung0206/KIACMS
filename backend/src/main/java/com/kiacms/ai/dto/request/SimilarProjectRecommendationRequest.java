package com.kiacms.ai.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SimilarProjectRecommendationRequest(
        @NotBlank
        @Size(max = 2000)
        String projectDescription,

        @Min(1)
        @Max(5)
        Integer limit
) {
}
