package com.kiacms.ai.dto.response;

import java.util.UUID;

public record ProjectRecommendationItemResponse(
        UUID projectPostId,
        String title,
        String similarityReason,
        String recommendedPosition
) {
}
