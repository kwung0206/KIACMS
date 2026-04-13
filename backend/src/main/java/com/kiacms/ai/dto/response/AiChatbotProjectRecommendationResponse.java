package com.kiacms.ai.dto.response;

import com.kiacms.project.enums.ProjectPostStatus;
import java.time.LocalDate;
import java.util.UUID;

public record AiChatbotProjectRecommendationResponse(
        UUID projectPostId,
        String title,
        String ownerName,
        ProjectPostStatus status,
        LocalDate recruitUntil,
        String reason,
        String recommendedPosition
) {
}
