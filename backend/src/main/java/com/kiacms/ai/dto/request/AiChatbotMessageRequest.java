package com.kiacms.ai.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record AiChatbotMessageRequest(
        @NotBlank
        @Size(max = 3000)
        String message,

        @Valid
        @Size(max = 12)
        List<ChatHistoryMessageRequest> history,

        @Size(max = 255)
        String currentPath
) {
    public record ChatHistoryMessageRequest(
            @NotBlank
            @Size(max = 20)
            String role,

            @NotBlank
            @Size(max = 2000)
            String content
    ) {
    }
}
