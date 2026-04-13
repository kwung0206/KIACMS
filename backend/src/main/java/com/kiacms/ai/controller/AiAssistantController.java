package com.kiacms.ai.controller;

import com.kiacms.ai.dto.request.AiChatbotMessageRequest;
import com.kiacms.ai.dto.request.ProjectPlanInsightRequest;
import com.kiacms.ai.dto.response.AiChatbotResponse;
import com.kiacms.ai.dto.response.ProjectPlanInsightResponse;
import com.kiacms.ai.service.ProjectPlanInsightAiService;
import com.kiacms.ai.service.SiteChatAiService;
import com.kiacms.global.exception.ResourceNotFoundException;
import com.kiacms.global.response.ApiResponse;
import com.kiacms.global.security.CustomUserPrincipal;
import com.kiacms.user.entity.User;
import com.kiacms.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiAssistantController {

    private final SiteChatAiService siteChatAiService;
    private final ProjectPlanInsightAiService projectPlanInsightAiService;
    private final UserRepository userRepository;

    @PostMapping("/chatbot/messages")
    public ApiResponse<AiChatbotResponse> sendChatbotMessage(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody AiChatbotMessageRequest request
    ) {
        return ApiResponse.ok(siteChatAiService.chat(request, getCurrentUser(principal)));
    }

    @PostMapping("/project-plan-insights")
    public ApiResponse<ProjectPlanInsightResponse> analyzeProjectPlan(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody ProjectPlanInsightRequest request
    ) {
        return ApiResponse.ok(projectPlanInsightAiService.analyze(request, getCurrentUser(principal)));
    }

    private User getCurrentUser(CustomUserPrincipal principal) {
        return userRepository.findByIdAndDeletedAtIsNull(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found."));
    }
}
