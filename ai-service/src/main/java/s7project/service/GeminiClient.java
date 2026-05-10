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
import java.util.List;
import java.util.Map;

@Component
public class GeminiClient {
    private static final String GEMINI_URL_TEMPLATE =
            "https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent";
    private static final Logger LOGGER = LoggerFactory.getLogger(GeminiClient.class);

    private final RestClient restClient;
    private final AiPromptBuilder promptBuilder;
    private final AiInsightResponseParser responseParser;
    private final String geminiApiKey;
    private final String geminiModel;

    public GeminiClient(
            RestClient restClient,
            AiPromptBuilder promptBuilder,
            AiInsightResponseParser responseParser,
            @Value("${gemini.api-key:}") String geminiApiKey,
            @Value("${gemini.model:gemini-2.5-flash}") String geminiModel
    ) {
        this.restClient = restClient;
        this.promptBuilder = promptBuilder;
        this.responseParser = responseParser;
        this.geminiApiKey = geminiApiKey;
        this.geminiModel = geminiModel;
    }

    public AiInsightResponse generate(AiInsightType type, AiRequest request) throws Exception {
        if (geminiApiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY is not configured");
        }

        LOGGER.info("Starting Gemini request. model={}, type={}, channelId={}", geminiModel, type.displayName(), request.channelId());

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("contents", List.of(Map.of(
                "parts", List.of(Map.of("text", promptBuilder.buildPrompt(type, request)))
        )));
        payload.put("generationConfig", Map.of(
                "responseMimeType", "application/json",
                "responseSchema", responseSchema()
        ));

        JsonNode response = restClient.post()
                .uri(GEMINI_URL_TEMPLATE, geminiModel)
                .header("x-goog-api-key", geminiApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(JsonNode.class);

        LOGGER.info("Gemini response received for channel {}. candidates={}", request.channelId(), response.path("candidates").size());

        String rawText = extractGeneratedText(response);

        try {
            return responseParser.parse(rawText, type, "Gemini");
        } catch (Exception exception) {
            LOGGER.warn("Gemini parse failure for channel {}. rawResponsePreview={}", request.channelId(), preview(rawText), exception);
            throw exception;
        }
    }

    private Map<String, Object> responseSchema() {
        return Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "title", Map.of("type", "STRING"),
                        "subtitle", Map.of("type", "STRING"),
                        "content", Map.of("type", "STRING"),
                        "bullets", Map.of(
                                "type", "ARRAY",
                                "items", Map.of("type", "STRING")
                        ),
                        "footer", Map.of("type", "STRING")
                ),
                "required", List.of("title", "subtitle", "content", "bullets", "footer")
        );
    }

    private String extractGeneratedText(JsonNode response) {
        JsonNode candidates = response.path("candidates");
        if (!candidates.isArray() || candidates.isEmpty()) {
            throw new IllegalStateException("Gemini response contained no candidates");
        }

        StringBuilder generatedText = new StringBuilder();
        JsonNode parts = candidates.path(0).path("content").path("parts");
        for (JsonNode part : parts) {
            String text = part.path("text").asText("");
            if (!text.isBlank()) {
                generatedText.append(text).append('\n');
            }
        }

        String combined = generatedText.toString().trim();
        if (combined.isBlank()) {
            throw new IllegalStateException("Gemini response did not contain text parts");
        }

        return combined;
    }

    private String preview(String text) {
        String normalized = text.replaceAll("\\s+", " ").trim();
        return normalized.length() > 240 ? normalized.substring(0, 240) + "..." : normalized;
    }
}
