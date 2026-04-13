package com.kiacms.ai.dto.response;

import java.util.List;

public record SimilarProjectRecommendationResponse(
        String projectIdea,
        List<ProjectRecommendationItemResponse> recommendations
) {
}
