package s7project.business;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import s7project.model.AiMessagePayload;
import s7project.model.AiInsightResponse;
import s7project.model.AiRequestPayload;
import s7project.model.ChannelResponse;
import s7project.model.MessageRequest;
import s7project.model.MessageResponse;
import s7project.persistence.ChatRepository;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;

@Service
public class ChatService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatService.class);

    private final ChatRepository chatRepository;
    private final RestClient restClient;
    private final String aiServiceBaseUrl;
    private final MockInsightGenerator mockInsightGenerator;

    public ChatService(
            ChatRepository chatRepository,
            RestClient restClient,
            MockInsightGenerator mockInsightGenerator,
            @Value("${AI_SERVICE_BASE_URL:http://localhost:8082}") String aiServiceBaseUrl
    ) {
        this.chatRepository = chatRepository;
        this.restClient = restClient;
        this.mockInsightGenerator = mockInsightGenerator;
        this.aiServiceBaseUrl = aiServiceBaseUrl;
    }

    public List<ChannelResponse> getChannels() {
        return chatRepository.getChannels();
    }

    public List<MessageResponse> getMessages(String channelId) {
        ensureChannelExists(channelId);
        return chatRepository.getMessages(channelId);
    }

    public MessageResponse createMessage(String channelId, MessageRequest request) {
        ensureChannelExists(channelId);

        String text = request.text() == null ? "" : request.text().trim();
        if (text.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message text is required");
        }

        return chatRepository.addMessage(channelId, text);
    }

    public AiInsightResponse getSummary(String channelId) {
        return generateInsight(channelId, AiInsightType.SUMMARY);
    }

    public AiInsightResponse getActionPoints(String channelId) {
        return generateInsight(channelId, AiInsightType.ACTION_POINTS);
    }

    public AiInsightResponse getDecisions(String channelId) {
        return generateInsight(channelId, AiInsightType.DECISIONS);
    }

    private void ensureChannelExists(String channelId) {
        boolean exists = chatRepository.getChannels().stream()
                .anyMatch(channel -> channel.id().equals(channelId));

        if (!exists) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Channel not found");
        }
    }

    private AiInsightResponse generateInsight(String channelId, AiInsightType type) {
        ChannelResponse channel = getChannel(channelId);
        List<MessageResponse> messages = getMessages(channelId);

        try {
            return restClient.post()
                    .uri(aiServiceBaseUrl + "/api/ai/" + type.path())
                    .body(toAiRequest(channel, messages))
                    .retrieve()
                    .body(AiInsightResponse.class);
        } catch (Exception exception) {
            LOGGER.warn("ai-service unavailable for {} on channel {}. Falling back to local mock output.",
                    type.path(), channelId, exception);
            simulateAnalysisDelay();
            return mockInsightGenerator.generate(type, channel, messages);
        }
    }

    private ChannelResponse getChannel(String channelId) {
        return chatRepository.getChannels().stream()
                .filter(channel -> channel.id().equals(channelId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Channel not found"));
    }

    private AiRequestPayload toAiRequest(ChannelResponse channel, List<MessageResponse> messages) {
        List<AiMessagePayload> payloadMessages = messages.stream()
                .map(message -> new AiMessagePayload(message.author(), message.text(), message.time()))
                .toList();

        return new AiRequestPayload(channel.id(), channel.name(), payloadMessages);
    }

    private void simulateAnalysisDelay() {
        try {
            Thread.sleep(450);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Analysis was interrupted");
        }
    }
}
