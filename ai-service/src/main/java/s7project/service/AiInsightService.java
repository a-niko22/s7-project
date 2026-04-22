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
        if ("gemini".equalsIgnoreCase(aiProvider)) {
            try {
                return geminiClient.generate(type, request);
            } catch (Exception exception) {
                LOGGER.warn("Gemini request failed for {}. Falling back to mock output.", type.displayName(), exception);
            }
        }

        return mockInsightGenerator.generate(type, request);
    }
}
