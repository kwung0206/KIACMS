package com.kiacms.ai.dto.response;

import java.util.UUID;

public record AiStructuredCourseReference(
        UUID courseId,
        String reason
) {
}
