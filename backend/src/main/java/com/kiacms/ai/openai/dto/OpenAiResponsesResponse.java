package com.kiacms.ai.openai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenAiResponsesResponse(
        String id,
        @JsonProperty("output_text")
        String outputText,
        List<OpenAiOutputItem> output,
        OpenAiUsage usage
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OpenAiOutputItem(
            String type,
            String refusal,
            List<OpenAiContentItem> content
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OpenAiContentItem(
            String type,
            String text,
            String refusal
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OpenAiUsage(
            @JsonProperty("input_tokens")
            Integer inputTokens,

            @JsonProperty("output_tokens")
            Integer outputTokens,

            @JsonProperty("total_tokens")
            Integer totalTokens
    ) {
    }
}
