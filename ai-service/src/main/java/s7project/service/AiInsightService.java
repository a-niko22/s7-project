package s7project.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import s7project.model.AiInsightResponse;
import s7project.model.AiRequest;

@Service
public class AiInsightService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AiInsightService.class);

    private final String aiProvider;
    private final GeminiClient geminiClient;
    private final OllamaClient ollamaClient;
    private final MockInsightGenerator mockInsightGenerator;

    public AiInsightService(
            @Value("${ai.provider:mock}") String aiProvider,
            GeminiClient geminiClient,
            OllamaClient ollamaClient,
            MockInsightGenerator mockInsightGenerator
    ) {
        this.aiProvider = aiProvider;
        this.geminiClient = geminiClient;
        this.ollamaClient = ollamaClient;
        this.mockInsightGenerator = mockInsightGenerator;
    }

    public AiInsightResponse generateSummary(AiRequest request) {
        return generate(AiInsightType.SUMMARY, request);
    }

    public AiInsightResponse generateActionPoints(AiRequest request) {
        return generate(AiInsightType.ACTION_POINTS, request);
    }

    public AiInsightResponse generateDecisions(AiRequest request) {
        return generate(AiInsightType.DECISIONS, request);
    }

    private AiInsightResponse generate(AiInsightType type, AiRequest request) {
        LOGGER.info(
                "AI request received. provider={}, type={}, channelId={}, channelName={}, messages={}",
                aiProvider,
                type.displayName(),
                request.channelId(),
                request.channelName(),
                request.messages() == null ? 0 : request.messages().size()
        );

        if ("mock".equalsIgnoreCase(aiProvider)) {
            return generateMock(type, request);
        }

        if ("gemini".equalsIgnoreCase(aiProvider)) {
            return generateWithFallback("Gemini", type, request, () -> geminiClient.generate(type, request));
        }

        if ("ollama".equalsIgnoreCase(aiProvider)) {
            return generateWithFallback("Ollama", type, request, () -> ollamaClient.generate(type, request));
        }

        LOGGER.warn("Unknown AI_PROVIDER '{}'. Falling back to mock output.", aiProvider);
        return generateMock(type, request);
    }

    private AiInsightResponse generateWithFallback(
            String providerName,
            AiInsightType type,
            AiRequest request,
            ProviderCall providerCall
    ) {
        try {
            AiInsightResponse providerResponse = providerCall.generate();
            LOGGER.info("{} succeeded for {} on channel {}", providerName, type.displayName(), request.channelId());
            return providerResponse;
        } catch (Exception exception) {
            LOGGER.warn(
                    "{} request failed for {} on channel {}. Falling back to mock output. reason={}",
                    providerName,
                    type.displayName(),
                    request.channelId(),
                    exception.getMessage(),
                    exception
            );
            return generateMock(type, request);
        }
    }

    private AiInsightResponse generateMock(AiInsightType type, AiRequest request) {
        LOGGER.info("Using ai-service mock output for {} on channel {}", type.displayName(), request.channelId());
        return mockInsightGenerator.generate(type, request);
    }

    @FunctionalInterface
    private interface ProviderCall {
        AiInsightResponse generate() throws Exception;
    }
}
