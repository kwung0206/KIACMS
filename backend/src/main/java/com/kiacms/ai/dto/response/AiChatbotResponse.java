package com.kiacms.ai.dto.response;

import com.kiacms.ai.enums.ChatbotIntentType;
import java.util.List;

public record AiChatbotResponse(
        ChatbotIntentType intentType,
        String answer,
        List<String> keywords,
        List<AiChatbotLinkResponse> suggestedLinks,
        List<String> followUpQuestions,
        List<AiChatbotCourseRecommendationResponse> recommendedCourses,
        List<AiChatbotProjectRecommendationResponse> recommendedProjects
) {
}
