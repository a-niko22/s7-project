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
    private final MockInsightGenerator mockInsightGenerator;

    public AiInsightService(
            @Value("${AI_PROVIDER:mock}") String aiProvider,
            GeminiClient geminiClient,
            MockInsightGenerator mockInsightGenerator
    ) {
        this.aiProvider = aiProvider;
        this.geminiClient = geminiClient;
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

        if ("gemini".equalsIgnoreCase(aiProvider)) {
            try {
                AiInsightResponse geminiResponse = geminiClient.generate(type, request);
                LOGGER.info("Gemini succeeded for {} on channel {}", type.displayName(), request.channelId());
                return geminiResponse;
            } catch (Exception exception) {
                LOGGER.warn(
                        "Gemini request failed for {} on channel {}. Falling back to mock output. reason={}",
                        type.displayName(),
                        request.channelId(),
                        exception.getMessage(),
                        exception
                );
            }
        }

        LOGGER.info("Using ai-service mock output for {} on channel {}", type.displayName(), request.channelId());
        return mockInsightGenerator.generate(type, request);
    }
}
