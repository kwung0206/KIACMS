package com.kiacms.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiacms.ai.entity.AiRequestLog;
import com.kiacms.ai.enums.AiFeatureType;
import com.kiacms.ai.enums.AiReferenceType;
import com.kiacms.ai.enums.AiRequestStatus;
import com.kiacms.ai.openai.dto.OpenAiResponsesResponse;
import com.kiacms.ai.repository.AiRequestLogRepository;
import com.kiacms.global.config.properties.OpenAiProperties;
import com.kiacms.global.exception.BusinessException;
import com.kiacms.global.exception.ErrorCode;
import com.kiacms.user.entity.User;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
@RequiredArgsConstructor
public class OpenAiStructuredResponseClient {

    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;
    private final AiRequestLogRepository aiRequestLogRepository;
    private final OpenAiProperties openAiProperties;

    public <T> T requestStructuredOutput(
            User requester,
            AiFeatureType featureType,
            AiReferenceType referenceType,
            UUID referenceId,
            String systemPrompt,
            String userPrompt,
            String schemaName,
            JsonNode schema,
            Class<T> responseType,
            int maxOutputTokens
    ) {
        validateConfiguration();

        AiRequestLog log = aiRequestLogRepository.save(AiRequestLog.builder()
                .requester(requester)
                .featureType(featureType)
                .status(AiRequestStatus.FAILED)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .modelName(openAiProperties.model())
                .promptVersion(openAiProperties.promptVersion())
                .requestPreview(truncateForLog(userPrompt))
                .build());

        try {
            OpenAiResponsesResponse response = restClient().post()
                    .uri("/responses")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + openAiProperties.apiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(buildRequestBody(systemPrompt, userPrompt, schemaName, schema, maxOutputTokens))
                    .retrieve()
                    .body(OpenAiResponsesResponse.class);

            if (response == null) {
                throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID, "OpenAI returned an empty response body.");
            }

            String jsonPayload = extractStructuredText(response);
            T parsedResponse = objectMapper.readValue(jsonPayload, responseType);

            log.setStatus(AiRequestStatus.SUCCESS);
            log.setResponsePreview(truncateForLog(jsonPayload));
            updateUsage(log, response.usage());
            log.setCompletedAt(Instant.now());
            aiRequestLogRepository.save(log);
            return parsedResponse;
        } catch (RestClientResponseException exception) {
            markFailure(log, exception.getResponseBodyAsString());
            throw new BusinessException(
                    ErrorCode.AI_PROVIDER_UNAVAILABLE,
                    "OpenAI request failed with status %d.".formatted(exception.getStatusCode().value())
            );
        } catch (RestClientException exception) {
            markFailure(log, exception.getMessage());
            throw new BusinessException(ErrorCode.AI_PROVIDER_UNAVAILABLE, "Failed to connect to OpenAI.");
        } catch (BusinessException exception) {
            markFailure(log, exception.getMessage());
            throw exception;
        } catch (Exception exception) {
            markFailure(log, exception.getMessage());
            throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID, "Failed to parse structured AI response.");
        }
    }

    private RestClient restClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(openAiProperties.timeoutSeconds()));
        requestFactory.setReadTimeout(Duration.ofSeconds(openAiProperties.timeoutSeconds()));

        return restClientBuilder
                .baseUrl(openAiProperties.baseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    private Map<String, Object> buildRequestBody(
            String systemPrompt,
            String userPrompt,
            String schemaName,
            JsonNode schema,
            int maxOutputTokens
    ) {
        return Map.of(
                "model", openAiProperties.model(),
                "store", false,
                "max_output_tokens", maxOutputTokens,
                "input", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "text", Map.of(
                        "format", Map.of(
                                "type", "json_schema",
                                "name", schemaName,
                                "strict", true,
                                "schema", schema
                        )
                )
        );
    }

    private String extractStructuredText(OpenAiResponsesResponse response) {
        if (response.outputText() != null && !response.outputText().isBlank()) {
            return response.outputText();
        }

        if (response.output() == null || response.output().isEmpty()) {
            throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID, "OpenAI returned no structured output.");
        }

        for (OpenAiResponsesResponse.OpenAiOutputItem outputItem : response.output()) {
            if (outputItem.refusal() != null && !outputItem.refusal().isBlank()) {
                throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID, outputItem.refusal());
            }

            if (outputItem.content() == null) {
                continue;
            }

            for (OpenAiResponsesResponse.OpenAiContentItem contentItem : outputItem.content()) {
                if (contentItem.refusal() != null && !contentItem.refusal().isBlank()) {
                    throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID, contentItem.refusal());
                }

                if (contentItem.text() != null && !contentItem.text().isBlank()) {
                    return contentItem.text();
                }
            }
        }

        throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID, "OpenAI returned output without text content.");
    }

    private void validateConfiguration() {
        if (!openAiProperties.enabled()) {
            throw new BusinessException(ErrorCode.AI_CONFIGURATION_ERROR, "AI features are disabled.");
        }
        if (openAiProperties.apiKey() == null || openAiProperties.apiKey().isBlank()) {
            throw new BusinessException(ErrorCode.AI_CONFIGURATION_ERROR, "OPENAI_API_KEY is not configured.");
        }
    }

    private void updateUsage(AiRequestLog log, OpenAiResponsesResponse.OpenAiUsage usage) {
        if (usage == null) {
            return;
        }
        log.setInputTokenCount(usage.inputTokens());
        log.setOutputTokenCount(usage.outputTokens());
        log.setTotalTokenCount(usage.totalTokens());
    }

    private void markFailure(AiRequestLog log, String errorMessage) {
        log.setStatus(AiRequestStatus.FAILED);
        log.setErrorMessage(truncateForLog(errorMessage));
        log.setCompletedAt(Instant.now());
        aiRequestLogRepository.save(log);
    }

    private String truncateForLog(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.length() <= 2000 ? value : value.substring(0, 2000) + "...";
    }
}
