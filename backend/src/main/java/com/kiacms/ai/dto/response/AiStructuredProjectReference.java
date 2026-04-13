package com.kiacms.ai.dto.response;

import java.util.UUID;

public record AiStructuredProjectReference(
        UUID projectPostId,
        String reason,
        String recommendedPosition
) {
}
