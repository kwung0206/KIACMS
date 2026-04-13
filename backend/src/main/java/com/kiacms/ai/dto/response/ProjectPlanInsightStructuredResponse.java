package com.kiacms.ai.dto.response;

import java.util.List;

public record ProjectPlanInsightStructuredResponse(
        String analysisSummary,
        List<String> keywords,
        List<AiStructuredCourseReference> recommendedCourses,
        List<AiStructuredProjectReference> similarProjects,
        String notificationMessage
) {
}
