package com.kiacms.ai.dto.response;

import java.util.List;
import java.util.UUID;

public record ProjectPlanInsightResponse(
        UUID projectPostId,
        String projectTitle,
        String analysisSummary,
        List<String> keywords,
        List<AiChatbotCourseRecommendationResponse> recommendedCourses,
        List<AiChatbotProjectRecommendationResponse> similarProjects,
        String notificationMessage,
        int matchedStudentCount,
        int notificationCount,
        List<ProjectPlanMatchedStudentResponse> matchedStudents
) {
}
