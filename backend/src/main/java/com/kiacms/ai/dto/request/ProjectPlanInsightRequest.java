package com.kiacms.ai.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ProjectPlanInsightRequest(
        UUID projectPostId,

        @Size(max = 5000)
        String projectPlanText,

        boolean sendNotifications,

        @Min(1)
        @Max(5)
        Integer limit
) {
}
