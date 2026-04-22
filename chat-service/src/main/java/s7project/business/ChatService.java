package s7project.business;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import s7project.model.AiInsightResponse;
import s7project.model.ChannelResponse;
import s7project.model.MessageRequest;
import s7project.model.MessageResponse;
import s7project.persistence.ChatRepository;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
        simulateAnalysisDelay();

        return switch (channelId) {
            case "general" -> createInsight(
                    "Summary",
                    "Team alignment",
                    "The team aligned on a Thursday demo centered on one stable collaboration screen. "
                            + "Everyone agreed the strongest story is channel navigation, realistic chat history, "
                            + "message sending, and a polished AI side panel that turns conversation into useful takeaways.",
                    List.of(
                            "Keep the scope intentionally narrow so the demo feels reliable",
                            "Use seeded conversations to make the AI recap look intentional",
                            "Favor polish and clarity over extra architecture or settings"
                    ),
                    messages
            );
            case "project-ideas" -> createInsight(
                    "Summary",
                    "Product framing",
                    "This discussion explored how the assistant should feel in the demo. "
                            + "The group kept coming back to one theme: the AI panel should read like a helpful project coordinator, "
                            + "not a generic chatbot.",
                    List.of(
                            "Show structured insights instead of a blob of text",
                            "Keep actions fixed and predictable for a live demo",
                            "Use realistic planning language so the output feels believable"
                    ),
                    messages
            );
            case "backend" -> createInsight(
                    "Summary",
                    "Implementation approach",
                    "The backend conversation settled on a lightweight implementation that supports the demo without extra moving parts. "
                            + "The service only needs in-memory data, stable endpoints, and deterministic AI responses that the frontend can render cleanly.",
                    List.of(
                            "Keep data in memory for quick iteration",
                            "Expose simple channel, message, and AI analysis endpoints",
                            "Avoid introducing infrastructure that adds setup risk before Thursday"
                    ),
                    messages
            );
            case "meeting-notes" -> createInsight(
                    "Summary",
                    "Recap of agreed plan",
                    "The notes channel captures the current demo contract: one presentable screen, believable seeded conversations, "
                            + "and AI outputs that clearly summarize progress, actions, and decisions.",
                    List.of(
                            "Demo one main channel experience",
                            "Use deterministic AI responses for consistency",
                            "Keep the run flow simple for local startup"
                    ),
                    messages
            );
            default -> createInsight(
                    "Summary",
                    "Channel recap",
                    "The team refined the main demo story for SyncSpace AI: an AI-assisted channel experience that helps people "
                            + "quickly understand what happened, what needs doing next, and what has already been decided.",
                    List.of(
                            "The Thursday demo should feel focused and easy to follow",
                            "The AI panel is part of the core value, not a side experiment",
                            "The latest discussion reinforces a presentable, low-risk implementation"
                    ),
                    messages
            );
        };
    }

    public AiInsightResponse getActionPoints(String channelId) {
        List<MessageResponse> messages = getMessages(channelId);
        simulateAnalysisDelay();

        return switch (channelId) {
            case "general" -> createInsight(
                    "Action Points",
                    "What the team should do next",
                    "The remaining work is mostly polish. The conversation suggests the team is already aligned on scope and now needs to tighten the live demo flow.",
                    List.of(
                            "Polish the right AI panel so each result looks demo-ready",
                            "Use the seeded channels to rehearse the story from channel switch to AI recap",
                            "Verify the app starts locally with minimal setup before the review"
                    ),
                    messages
            );
            case "project-ideas" -> createInsight(
                    "Action Points",
                    "Product follow-up",
                    "The team identified a few focused follow-ups to make the AI assistant feel more intentional during the demo.",
                    List.of(
                            "Phrase AI outputs like a meeting recap rather than a generic assistant response",
                            "Highlight action owners and deadlines when they are mentioned in the discussion",
                            "Keep the panel layout scannable so people can read it from a distance"
                    ),
                    messages
            );
            case "backend" -> createInsight(
                    "Action Points",
                    "Delivery checklist",
                    "The backend thread points to a short, low-risk checklist that supports the UI without broadening scope.",
                    List.of(
                            "Keep the seeded data realistic across all channels",
                            "Return structured AI fields the frontend can style consistently",
                            "Leave the service deterministic so the same demo clicks always produce the same story"
                    ),
                    messages
            );
            case "meeting-notes" -> createInsight(
                    "Action Points",
                    "Next steps from notes",
                    "The notes point to practical follow-up items for the Thursday walkthrough.",
                    List.of(
                            "Confirm the demo script: switch channel, review discussion, send a message, run AI analysis",
                            "Double-check that the seeded copy supports clear summaries and decisions",
                            "Keep the screen clean enough that new viewers understand the layout immediately"
                    ),
                    messages
            );
            default -> createInsight(
                    "Action Points",
                    "Suggested follow-up",
                    "The discussion already converges on what needs to happen next, and the remaining work is very manageable.",
                    List.of(
                            "Finish the presentational polish around AI cards and loading states",
                            "Use seeded project planning conversations to make the outputs feel grounded",
                            "Test the main demo path end to end before Thursday"
                    ),
                    messages
            );
        };
    }

    public AiInsightResponse getDecisions(String channelId) {
        List<MessageResponse> messages = getMessages(channelId);
        simulateAnalysisDelay();

        return switch (channelId) {
            case "general" -> createInsight(
                    "Decisions",
                    "Confirmed direction",
                    "The core demo direction is now settled. The team chose a smaller, clearer story that can be shown confidently in one live pass.",
                    List.of(
                            "Show one polished chat screen instead of a broader workspace",
                            "Keep AI analysis as the standout feature on the right side of the layout",
                            "Optimize for reliability and readability over extra features"
                    ),
                    messages
            );
            case "project-ideas" -> createInsight(
                    "Decisions",
                    "Product choices",
                    "The product discussion narrowed the assistant behavior into something practical for a demo.",
                    List.of(
                            "Present the AI as a conversation support tool, not an open-ended chat bot",
                            "Use structured recap cards for summary, action points, and decisions",
                            "Save bigger ideas like long-term memory for later iterations"
                    ),
                    messages
            );
            case "backend" -> createInsight(
                    "Decisions",
                    "Implementation decisions",
                    "The backend scope is intentionally constrained so the demo remains easy to run and explain.",
                    List.of(
                            "Keep all demo data in memory",
                            "Expose only the endpoints needed by the frontend demo",
                            "Do not add infrastructure like databases, queues, or sockets before Thursday"
                    ),
                    messages
            );
            case "meeting-notes" -> createInsight(
                    "Decisions",
                    "Recorded decisions",
                    "These notes capture the decisions the team wants to repeat consistently during the walkthrough.",
                    List.of(
                            "Demo seeded channels with realistic planning conversations",
                            "Use deterministic AI analysis so the same clicks always tell the same story",
                            "Keep the implementation lightweight and local-run friendly"
                    ),
                    messages
            );
            default -> createInsight(
                    "Decisions",
                    "Current decisions",
                    "The conversation makes the main choices clear and keeps the implementation tightly focused.",
                    List.of(
                            "Lead with AI-assisted conversation support in the demo",
                            "Use believable seeded discussion instead of empty-state UX",
                            "Avoid additional platform work until after the Thursday review"
                    ),
                    messages
            );
        };
    }

    private void ensureChannelExists(String channelId) {
        boolean exists = chatRepository.getChannels().stream()
                .anyMatch(channel -> channel.id().equals(channelId));

        if (!exists) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Channel not found");
        }
    }

    private AiInsightResponse createInsight(
            String title,
            String subtitle,
            String content,
            List<String> bullets,
            List<MessageResponse> messages
    ) {
        Set<String> participants = new LinkedHashSet<>();
        for (MessageResponse message : messages) {
            participants.add(message.author());
        }

        String footer = "Based on " + messages.size() + " messages from " + participants.size() + " participants.";
        return new AiInsightResponse(title, subtitle, content, bullets, footer);
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
