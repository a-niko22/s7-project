package s7project.business;

import org.springframework.stereotype.Component;
import s7project.model.AiInsightResponse;
import s7project.model.ChannelResponse;
import s7project.model.MessageResponse;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class MockInsightGenerator {

    public AiInsightResponse generate(AiInsightType type, ChannelResponse channel, List<MessageResponse> messages) {
        return switch (type) {
            case SUMMARY -> buildSummary(channel, messages);
            case ACTION_POINTS -> buildActionPoints(channel, messages);
            case DECISIONS -> buildDecisions(channel, messages);
        };
    }

    private AiInsightResponse buildSummary(ChannelResponse channel, List<MessageResponse> messages) {
        return createInsight(
                "Summary",
                "Conversation recap",
                "This channel is focused on " + channel.description() + ". The discussion is converging on a clear demo plan, "
                        + "with the team aligning on what to show, what to keep simple, and how the assistant should help people understand the thread quickly.",
                List.of(
                        "The conversation centers on a small, polished demo scope",
                        "The AI assistant is expected to summarize progress and clarify next steps",
                        "The latest messages reinforce a stable, demo-ready implementation path"
                ),
                channel,
                messages
        );
    }

    private AiInsightResponse buildActionPoints(ChannelResponse channel, List<MessageResponse> messages) {
        String latestQuestion = messages.stream()
                .map(MessageResponse::text)
                .filter(text -> text != null && text.contains("?"))
                .reduce((previous, current) -> current)
                .orElse("Confirm the final demo checklist and owners.");

        return createInsight(
                "Action Points",
                "Suggested follow-up",
                "These are the clearest next steps surfaced from the discussion.",
                List.of(
                        "Polish the main demo flow so channel switching, messaging, and AI analysis feel seamless",
                        "Keep the seeded conversation realistic enough to produce convincing AI output",
                        "Resolve the open question: " + latestQuestion
                ),
                channel,
                messages
        );
    }

    private AiInsightResponse buildDecisions(ChannelResponse channel, List<MessageResponse> messages) {
        return createInsight(
                "Decisions",
                "Confirmed outcomes",
                "The discussion already reflects a few decisions that appear settled enough to present in a live demo.",
                List.of(
                        "Prioritize one polished channel experience instead of expanding the product surface area",
                        "Keep the AI output structured and easy to scan in the side panel",
                        "Avoid adding infrastructure that could increase setup or demo risk"
                ),
                channel,
                messages
        );
    }

    private AiInsightResponse createInsight(
            String title,
            String subtitle,
            String content,
            List<String> bullets,
            ChannelResponse channel,
            List<MessageResponse> messages
    ) {
        Set<String> participants = new LinkedHashSet<>();
        for (MessageResponse message : messages) {
            if (message.author() != null && !message.author().isBlank()) {
                participants.add(message.author());
            }
        }

        String footer = "Based on " + messages.size() + " messages from " + participants.size()
                + " participants in #" + channel.name() + ".";

        return new AiInsightResponse(title, subtitle, content, bullets, footer);
    }
}
