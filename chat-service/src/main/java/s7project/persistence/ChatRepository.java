package s7project.persistence;

import org.springframework.stereotype.Repository;
import s7project.model.ChannelResponse;
import s7project.model.MessageResponse;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Repository
public class ChatRepository {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);

    private final List<ChannelResponse> channels = List.of(
            new ChannelResponse("general", "general", "Team-wide updates"),
            new ChannelResponse("project-ideas", "project-ideas", "Product experiments"),
            new ChannelResponse("ai-design", "ai-design", "Project discussion"),
            new ChannelResponse("backend", "backend", "API and integration notes"),
            new ChannelResponse("meeting-notes", "meeting-notes", "Recaps and follow-ups")
    );

    private final Map<String, List<MessageResponse>> messagesByChannel = new LinkedHashMap<>();

    public ChatRepository() {
        seedMessages();
    }

    public List<ChannelResponse> getChannels() {
        return channels;
    }

    public List<MessageResponse> getMessages(String channelId) {
        List<MessageResponse> messages = messagesByChannel.get(channelId);
        return messages == null ? List.of() : List.copyOf(messages);
    }

    public MessageResponse addMessage(String channelId, String text) {
        MessageResponse newMessage = new MessageResponse(
                UUID.randomUUID().toString(),
                "You",
                text,
                LocalTime.now().format(TIME_FORMATTER)
        );

        messagesByChannel.computeIfAbsent(channelId, ignored -> new ArrayList<>()).add(newMessage);
        return newMessage;
    }

    private void seedMessages() {
        messagesByChannel.put("general", new ArrayList<>(List.of(
                new MessageResponse("g-1", "Mia", "Thursday demo focus: one excellent chat screen over breadth.", "9:10 AM"),
                new MessageResponse("g-2", "Alex", "If we nail channel switching, messaging, and AI recap, that should be enough.", "9:18 AM")
        )));

        messagesByChannel.put("project-ideas", new ArrayList<>(List.of(
                new MessageResponse("p-1", "Dina", "Could the AI panel become a persistent project memory later on?", "8:42 AM"),
                new MessageResponse("p-2", "Sam", "Yes, but for now we only need fixed actions that feel useful in the demo.", "8:55 AM")
        )));

        messagesByChannel.put("ai-design", new ArrayList<>(List.of(
                new MessageResponse("a-1", "You", "Starting to think about the MVP scope. I want to keep this focused on AI-assisted conversation support first.", "10:23 AM"),
                new MessageResponse("a-2", "You", "The core value prop is having AI summarize discussions, catch you up when you've been away, and extract key decisions and action points.", "10:25 AM"),
                new MessageResponse("a-3", "You", "For the backend, I'm thinking RabbitMQ makes sense for async communication between the chat service and the AI processing service.", "10:31 AM"),
                new MessageResponse("a-4", "You", "Question: should the AI summarization happen on-demand only, or should it run automatically at certain intervals?", "10:45 AM"),
                new MessageResponse("a-5", "You", "I think on-demand makes more sense for the MVP. Users click 'Catch me up' and get a personalized recap.", "11:02 AM"),
                new MessageResponse("a-6", "You", "Adding to backlog: extract action points from conversations. This could be super useful for async teams.", "11:15 AM"),
                new MessageResponse("a-7", "You", "What did we decide about the Kubernetes deployment approach? Should I keep it simple with Docker Compose for the prototype?", "11:28 AM"),
                new MessageResponse("a-8", "You", "Content moderation is important but probably not MVP. The AI features should come first since that's the differentiator.", "11:45 AM"),
                new MessageResponse("a-9", "You", "So to recap: AI summarization, recaps, action points, and question answering are the core MVP features. Moderation can be an optional future extension.", "12:01 PM")
        )));

        messagesByChannel.put("backend", new ArrayList<>(List.of(
                new MessageResponse("b-1", "Chris", "Let's keep the demo service in-memory and revisit persistence after Thursday.", "1:05 PM"),
                new MessageResponse("b-2", "Nina", "Agreed. Stable APIs matter more than infrastructure right now.", "1:12 PM")
        )));

        messagesByChannel.put("meeting-notes", new ArrayList<>(List.of(
                new MessageResponse("m-1", "PM Bot", "Decision log: one main screen, seeded channels, demo AI insights, no auth.", "2:00 PM"),
                new MessageResponse("m-2", "PM Bot", "Action: connect frontend to chat-service and verify local run flow.", "2:03 PM")
        )));
    }
}
