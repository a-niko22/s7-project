package s7project.business;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import s7project.model.AiInsightResponse;
import s7project.model.ChannelResponse;
import s7project.model.MessageRequest;
import s7project.model.MessageResponse;
import s7project.persistence.ChatRepository;

import java.util.List;

@Service
public class ChatService {
    private final ChatRepository chatRepository;

    public ChatService(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
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
        List<MessageResponse> messages = getMessages(channelId);
        String latestMessage = messages.get(messages.size() - 1).text();

        String content = "This channel is focused on MVP planning for SyncSpace AI. "
                + "The team is aligning on a demo-first chat experience with AI support, and the latest update is: "
                + latestMessage;

        return new AiInsightResponse("Summary", content, List.of(
                "Demo-first scope with one primary conversation screen",
                "AI is being used to recap discussion and extract signal from the thread"
        ));
    }

    public AiInsightResponse getActionPoints(String channelId) {
        List<MessageResponse> messages = getMessages(channelId);
        String mostRecentQuestion = messages.stream()
                .filter(message -> message.text().contains("?"))
                .reduce((previous, current) -> current)
                .map(MessageResponse::text)
                .orElse("Confirm the next set of delivery tasks for the MVP demo.");

        return new AiInsightResponse("Action Points", "Suggested follow-ups pulled from the conversation.", List.of(
                "Create the clickable wireframe demo for the Thursday review",
                "Keep backend storage in memory and expose the channel/message API",
                "Resolve the open question: " + mostRecentQuestion
        ));
    }

    public AiInsightResponse getDecisions(String channelId) {
        getMessages(channelId);

        return new AiInsightResponse("Decisions", "Current decisions inferred from the seeded discussion.", List.of(
                "Prioritize one polished channel screen instead of a full workspace",
                "Keep AI features lightweight with demo endpoints for summary, actions, and decisions",
                "Avoid infrastructure work like auth, databases, and message brokers for this prototype"
        ));
    }

    private void ensureChannelExists(String channelId) {
        boolean exists = chatRepository.getChannels().stream()
                .anyMatch(channel -> channel.id().equals(channelId));

        if (!exists) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Channel not found");
        }
    }
}
