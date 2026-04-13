package com.kiacms.global.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai.openai")
public record OpenAiProperties(
        boolean enabled,
        String apiKey,
        String baseUrl,
        String model,
        int timeoutSeconds,
        String promptVersion
) {
}
