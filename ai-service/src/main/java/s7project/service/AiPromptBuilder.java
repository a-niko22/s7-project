package s7project.service;

import org.springframework.stereotype.Component;
import s7project.model.AiMessageRequest;
import s7project.model.AiRequest;

import java.util.List;

@Component
public class AiPromptBuilder {

    public String buildPrompt(AiInsightType type, AiRequest request) {
        List<AiMessageRequest> allMessages =
                request.messages() == null ? List.of() : request.messages();
        List<AiMessageRequest> recentMessages = takeLast(allMessages, 8);
        List<AiMessageRequest> transcriptMessages = takeLast(allMessages, 40);

        StringBuilder prompt = new StringBuilder();
        prompt.append("You are helping prepare a product demo.\n");
        prompt.append("Analyze the following chat channel and respond in JSON only.\n");
        prompt.append("Return exactly these fields: title, subtitle, content, bullets, footer.\n");
        prompt.append("The bullets field must be an array of strings.\n");
        prompt.append("Do not wrap the response in markdown or code fences.\n");
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
            AiMessageRequest latestMessage = allMessages.get(allMessages.size() - 1);
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

    private List<AiMessageRequest> takeLast(List<AiMessageRequest> messages, int count) {
        if (messages.size() <= count) {
            return messages;
        }
        return messages.subList(messages.size() - count, messages.size());
    }

    private void appendMessage(StringBuilder prompt, AiMessageRequest message) {
        prompt.append("- [")
                .append(message.time())
                .append("] ")
                .append(message.author())
                .append(": ")
                .append(message.text())
                .append("\n");
    }
}
