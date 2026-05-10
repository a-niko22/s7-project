package s7project.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import s7project.model.AiInsightResponse;
import s7project.model.AiRequest;
import tools.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class OllamaClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(OllamaClient.class);

    private final RestClient restClient;
    private final AiPromptBuilder promptBuilder;
    private final AiInsightResponseParser responseParser;
    private final String ollamaBaseUrl;
    private final String ollamaModel;

    public OllamaClient(
            RestClient restClient,
            AiPromptBuilder promptBuilder,
            AiInsightResponseParser responseParser,
            @Value("${ollama.base-url:http://localhost:11434}") String ollamaBaseUrl,
            @Value("${ollama.model:llama3.1:8b}") String ollamaModel
    ) {
        this.restClient = restClient;
        this.promptBuilder = promptBuilder;
        this.responseParser = responseParser;
        this.ollamaBaseUrl = ollamaBaseUrl;
        this.ollamaModel = ollamaModel;
    }

    public AiInsightResponse generate(AiInsightType type, AiRequest request) throws Exception {
        LOGGER.info("Starting Ollama request. model={}, type={}, channelId={}", ollamaModel, type.displayName(), request.channelId());

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", ollamaModel);
        payload.put("prompt", promptBuilder.buildPrompt(type, request));
        payload.put("stream", false);
        payload.put("format", "json");

        JsonNode response = restClient.post()
                .uri(ollamaBaseUrl + "/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(JsonNode.class);

        String rawText = extractGeneratedText(response);

        try {
            return responseParser.parse(rawText, type, "Ollama");
        } catch (Exception exception) {
            LOGGER.warn("Ollama parse failure for channel {}. rawResponsePreview={}", request.channelId(), preview(rawText), exception);
            throw new IllegalArgumentException("Ollama response did not match the expected AI insight JSON shape", exception);
        }
    }

    private String extractGeneratedText(JsonNode response) {
        String generatedText = response.path("response").asText("").trim();
        if (generatedText.isBlank()) {
            throw new IllegalStateException("Ollama response did not contain a response field");
        }
        return generatedText;
    }

    private String preview(String text) {
        String normalized = text.replaceAll("\\s+", " ").trim();
        return normalized.length() > 240 ? normalized.substring(0, 240) + "..." : normalized;
    }
}
