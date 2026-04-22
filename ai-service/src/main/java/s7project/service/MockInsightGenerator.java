package s7project.service;

import org.springframework.stereotype.Component;
import s7project.model.AiInsightResponse;
import s7project.model.AiMessageRequest;
import s7project.model.AiRequest;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class MockInsightGenerator {

    public AiInsightResponse generate(AiInsightType type, AiRequest request) {
        return switch (type) {
            case SUMMARY -> buildSummary(request);
            case ACTION_POINTS -> buildActionPoints(request);
            case DECISIONS -> buildDecisions(request);
        };
    }

    private AiInsightResponse buildSummary(AiRequest request) {
        String channelName = normalizeChannelName(request.channelName());
        return createInsight(
                "[AI-SERVICE MOCK] Summary",
                "Conversation recap",
                "This channel is focused on " + channelName + ". The discussion is converging on a clear demo plan, "
                        + "with the team aligning on what to show, what to keep simple, and how the assistant should help viewers understand the thread quickly.",
                List.of(
                        "The conversation centers on a small, polished demo scope",
                        "The AI assistant is expected to summarize progress and clarify next steps",
                        "The latest messages reinforce a stable, demo-ready implementation path"
                ),
                request
        );
    }

    private AiInsightResponse buildActionPoints(AiRequest request) {
        String latestQuestion = request.messages().stream()
                .map(AiMessageRequest::text)
                .filter(text -> text != null && text.contains("?"))
                .reduce((previous, current) -> current)
                .orElse("Confirm the final demo checklist and owners.");

        return createInsight(
                "[AI-SERVICE MOCK] Action Points",
                "Suggested follow-up",
                "These are the clearest next steps surfaced from the discussion.",
                List.of(
                        "Polish the main demo flow so channel switching, messaging, and AI analysis feel seamless",
                        "Keep the seeded conversation realistic enough to produce convincing AI output",
                        "Resolve the open question: " + latestQuestion
                ),
                request
        );
    }

    private AiInsightResponse buildDecisions(AiRequest request) {
        return createInsight(
                "[AI-SERVICE MOCK] Decisions",
                "Confirmed outcomes",
                "The discussion already reflects a few decisions that appear settled enough to present in a live demo.",
                List.of(
                        "Prioritize one polished channel experience instead of expanding the product surface area",
                        "Keep the AI output structured and easy to scan in the side panel",
                        "Avoid adding infrastructure that could increase setup or demo risk"
                ),
                request
        );
    }

    private AiInsightResponse createInsight(
            String title,
            String subtitle,
            String content,
            List<String> bullets,
            AiRequest request
    ) {
        Set<String> participants = new LinkedHashSet<>();
        for (AiMessageRequest message : request.messages()) {
            if (message.author() != null && !message.author().isBlank()) {
                participants.add(message.author());
            }
        }

        String footer = "Based on " + request.messages().size() + " messages from " + participants.size()
                + " participants in #" + normalizeChannelName(request.channelName()) + ".";

        return new AiInsightResponse(title, subtitle, content, bullets, footer);
    }

    private String normalizeChannelName(String channelName) {
        return channelName == null || channelName.isBlank() ? "this conversation" : channelName;
    }
}
