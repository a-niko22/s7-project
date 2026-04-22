package s7project.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import s7project.model.AiInsightResponse;
import s7project.model.AiRequest;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class GeminiClient {
    private static final String GEMINI_URL_TEMPLATE =
            "https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String geminiApiKey;
    private final String geminiModel;

    public GeminiClient(
            RestClient restClient,
            ObjectMapper objectMapper,
            @Value("${GEMINI_API_KEY:}") String geminiApiKey,
            @Value("${GEMINI_MODEL:gemini-2.5-flash}") String geminiModel
    ) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.geminiApiKey = geminiApiKey;
        this.geminiModel = geminiModel;
    }

    public AiInsightResponse generate(AiInsightType type, AiRequest request) throws Exception {
        if (geminiApiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY is not configured");
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("contents", List.of(Map.of(
                "parts", List.of(Map.of("text", buildPrompt(type, request)))
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

        String jsonText = response.path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text")
                .asText();

        return objectMapper.readValue(jsonText, AiInsightResponse.class);
    }

    private String buildPrompt(AiInsightType type, AiRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are helping prepare a product demo.\n");
        prompt.append("Analyze the following chat channel and respond in JSON only.\n");
        prompt.append("Return fields: title, subtitle, content, bullets, footer.\n");
        prompt.append("Keep bullets concise, concrete, and suitable for a project demo UI.\n");
        prompt.append("Do not mention that you are an AI model.\n");
        prompt.append("Analysis type: ").append(type.displayName()).append("\n");
        prompt.append("Channel ID: ").append(request.channelId()).append("\n");
        prompt.append("Channel name: ").append(request.channelName()).append("\n");
        prompt.append("Messages:\n");

        request.messages().stream()
                .limit(20)
                .forEach(message -> prompt.append("- [")
                        .append(message.time())
                        .append("] ")
                        .append(message.author())
                        .append(": ")
                        .append(message.text())
                        .append("\n"));

        prompt.append("\n");
        prompt.append(switch (type) {
            case SUMMARY -> "Write a short recap with a paragraph-style content field and 2-3 supporting bullets.";
            case ACTION_POINTS -> "Write a short overview and list the clearest next steps as bullets.";
            case DECISIONS -> "Write a short overview and list the decisions that appear settled as bullets.";
        });

        return prompt.toString();
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
}
