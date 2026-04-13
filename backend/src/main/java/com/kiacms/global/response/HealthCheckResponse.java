package com.kiacms.global.response;

import java.time.Instant;

public record HealthCheckResponse(
        String applicationName,
        String status,
        Instant serverTime
) {
}
