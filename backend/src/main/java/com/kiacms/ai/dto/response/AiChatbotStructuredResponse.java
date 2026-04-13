package com.kiacms.ai.dto.response;

import com.kiacms.ai.enums.ChatbotIntentType;
import java.util.List;

public record AiChatbotStructuredResponse(
        ChatbotIntentType intentType,
        String answer,
        List<String> keywords,
        List<String> suggestedRouteKeys,
        List<String> followUpQuestions,
        List<AiStructuredCourseReference> recommendedCourses,
        List<AiStructuredProjectReference> recommendedProjects
) {
}
