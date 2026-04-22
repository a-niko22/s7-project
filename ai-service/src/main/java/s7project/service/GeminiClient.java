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
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class GeminiClient {
    private static final String GEMINI_URL_TEMPLATE =
            "https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent";
    private static final Logger LOGGER = LoggerFactory.getLogger(GeminiClient.class);

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

        LOGGER.info("Starting Gemini request. model={}, type={}, channelId={}", geminiModel, type.displayName(), request.channelId());

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

        LOGGER.info("Gemini response received for channel {}. candidates={}", request.channelId(), response.path("candidates").size());

        String rawText = extractGeneratedText(response);

        try {
            return parseInsightResponse(rawText, type);
        } catch (Exception exception) {
            LOGGER.warn("Gemini parse failure for channel {}. rawResponsePreview={}", request.channelId(), preview(rawText), exception);
            throw exception;
        }
    }

    private String buildPrompt(AiInsightType type, AiRequest request) {
        List<s7project.model.AiMessageRequest> allMessages =
                request.messages() == null ? List.of() : request.messages();
        List<s7project.model.AiMessageRequest> recentMessages = takeLast(allMessages, 8);
        List<s7project.model.AiMessageRequest> transcriptMessages = takeLast(allMessages, 40);

        StringBuilder prompt = new StringBuilder();
        prompt.append("You are helping prepare a product demo.\n");
        prompt.append("Analyze the following chat channel and respond in JSON only.\n");
        prompt.append("Return fields: title, subtitle, content, bullets, footer.\n");
        prompt.append("Keep bullets concise, concrete, and suitable for a project demo UI.\n");
        prompt.append("Treat the most recent messages as the highest-priority context.\n");
        prompt.append("If the latest messages introduce a new topic, scope change, decision, or phase, mention it explicitly.\n");
        prompt.append("Preserve unusual tokens, identifiers, or phrases from recent messages verbatim when they matter.\n");
        prompt.append("Do not mention that you are an AI model.\n");
        prompt.append("Analysis type: ").append(type.displayName()).append("\n");
        prompt.append("Channel ID: ").append(request.channelId()).append("\n");
        prompt.append("Channel name: ").append(request.channelName()).append("\n");
        prompt.append("Total messages available: ").append(allMessages.size()).append("\n");

        if (!allMessages.isEmpty()) {
            s7project.model.AiMessageRequest latestMessage = allMessages.get(allMessages.size() - 1);
            prompt.append("Latest message to consider carefully:\n");
            appendMessage(prompt, latestMessage);
            prompt.append("\n");
        }

        prompt.append("Most recent messages (highest priority, newest last):\n");
        recentMessages.forEach(message -> appendMessage(prompt, message));

        prompt.append("\nRecent transcript window:\n");
        transcriptMessages.forEach(message -> appendMessage(prompt, message));

        prompt.append("\n");
        prompt.append(switch (type) {
            case SUMMARY -> "Write a short recap with a paragraph-style content field and 2-3 supporting bullets. "
                    + "If the newest messages add a new topic or scope change, explicitly include it in the summary.";
            case ACTION_POINTS -> "Write a short overview and list the clearest next steps as bullets. "
                    + "Give extra weight to asks or follow-ups in the newest messages.";
            case DECISIONS -> "Write a short overview and list the decisions that appear settled as bullets. "
                    + "If the newest messages revise a decision, reflect the revision.";
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

    private AiInsightResponse parseInsightResponse(String rawText, AiInsightType type) throws Exception {
        String cleanedText = stripCodeFences(rawText).trim();

        try {
            return toInsightResponse(objectMapper.readTree(cleanedText), type);
        } catch (Exception ignored) {
            String extractedJson = extractJsonObject(cleanedText);
            if (extractedJson == null) {
                throw ignored;
            }

            return toInsightResponse(objectMapper.readTree(extractedJson), type);
        }
    }

    private AiInsightResponse toInsightResponse(JsonNode node, AiInsightType type) {
        String title = node.path("title").asText(type.displayName()).trim();
        String subtitle = node.path("subtitle").asText("Generated from the latest conversation").trim();
        String content = node.path("content").asText("").trim();
        String footer = node.path("footer").asText("Generated by Gemini.").trim();

        List<String> bullets = objectMapper.convertValue(
                node.path("bullets"),
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
        );

        return new AiInsightResponse(
                prefixGeminiTitle(title),
                subtitle.isBlank() ? "Generated from the latest conversation" : subtitle,
                content,
                bullets == null ? List.of() : bullets.stream().filter(bullet -> bullet != null && !bullet.isBlank()).toList(),
                footer.isBlank() ? "Generated by Gemini." : footer
        );
    }

    private String prefixGeminiTitle(String title) {
        return title.startsWith("[GEMINI]") ? title : "[GEMINI] " + title;
    }

    private String stripCodeFences(String text) {
        String normalized = text.trim();
        if (normalized.startsWith("```")) {
            normalized = normalized.replaceFirst("^```(?:json)?\\s*", "");
            normalized = normalized.replaceFirst("\\s*```$", "");
        }
        return normalized;
    }

    private String extractJsonObject(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return null;
        }
        return text.substring(start, end + 1);
    }

    private String preview(String text) {
        String normalized = text.replaceAll("\\s+", " ").trim();
        return normalized.length() > 240 ? normalized.substring(0, 240) + "..." : normalized;
    }

    private List<s7project.model.AiMessageRequest> takeLast(List<s7project.model.AiMessageRequest> messages, int count) {
        if (messages.size() <= count) {
            return messages;
        }
        return messages.subList(messages.size() - count, messages.size());
    }

    private void appendMessage(StringBuilder prompt, s7project.model.AiMessageRequest message) {
        prompt.append("- [")
                .append(message.time())
                .append("] ")
                .append(message.author())
                .append(": ")
                .append(message.text())
                .append("\n");
    }
}
