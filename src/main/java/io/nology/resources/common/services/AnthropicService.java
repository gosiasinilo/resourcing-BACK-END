package io.nology.resources.common.services;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AnthropicService {

    @Value("${anthropic.api.key:}")
    private String apiKey;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public AnthropicService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.anthropic.com")
                .build();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ContentBlock(String type, String text) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record AnthropicResponse(List<ContentBlock> content) {}

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String complete(String prompt) {
        var body = Map.of(
            "model", "claude-haiku-4-5-20251001",
            "max_tokens", 512,
            "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        AnthropicResponse resp = restClient.post()
                .uri("/v1/messages")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .body(AnthropicResponse.class);

        if (resp == null || resp.content() == null || resp.content().isEmpty()) {
            throw new RuntimeException("Empty response from Anthropic");
        }
        return resp.content().get(0).text();
    }

    public record TempRecommendation(Long tempId, String reasoning) {}

    public TempRecommendation recommendTemp(String prompt) {
        String raw = complete(prompt);
        String json = raw.trim();
        int start = json.indexOf('{');
        int end = json.lastIndexOf('}');
        if (start >= 0 && end > start) {
            json = json.substring(start, end + 1);
        }
        try {
            var node = objectMapper.readTree(json);
            Long tempId = node.get("tempId").asLong();
            String reasoning = node.get("reasoning").asText();
            return new TempRecommendation(tempId, reasoning);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse recommendation: " + raw);
        }
    }
}
