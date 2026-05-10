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
            new ChannelResponse("general", "general", "Team-wide alignment for the Thursday walkthrough"),
            new ChannelResponse("project-ideas", "project-ideas", "How the assistant should feel in the demo"),
            new ChannelResponse("ai-design", "ai-design", "Main planning thread for the demo experience"),
            new ChannelResponse("backend", "backend", "Delivery plan for the demo API"),
            new ChannelResponse("meeting-notes", "meeting-notes", "Recaps, owners, and agreed decisions")
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
                new MessageResponse("g-1", "Mia", "For Thursday, I want us to demo one screen really well instead of trying to show a whole collaboration suite.", "9:02 AM"),
                new MessageResponse("g-2", "Alex", "Agreed. The strongest flow is: open a channel, scan the discussion, send a message, then use AI to summarize what changed.", "9:08 AM"),
                new MessageResponse("g-3", "Jordan", "That means the seeded conversation quality matters a lot. The AI output will feel fake if the messages are too generic.", "9:12 AM"),
                new MessageResponse("g-4", "Mia", "Let's make the content sound like a real project team deciding what to show, who owns what, and what we intentionally cut.", "9:18 AM"),
                new MessageResponse("g-5", "Alex", "Perfect. If the UI looks clean and the recap cards feel useful, that's enough to land the story.", "9:22 AM")
        )));

        messagesByChannel.put("project-ideas", new ArrayList<>(List.of(
                new MessageResponse("p-1", "Dina", "The AI panel should feel like a project coordinator, not like a chatbot stuck on the side of the screen.", "8:35 AM"),
                new MessageResponse("p-2", "Sam", "Yes. For the demo, every action should produce something structured and immediately readable.", "8:39 AM"),
                new MessageResponse("p-3", "Dina", "Summary should answer 'what happened', action points should answer 'what happens next', and decisions should answer 'what is already settled'.", "8:44 AM"),
                new MessageResponse("p-4", "Sam", "Exactly. If a viewer can read the panel from a distance and understand the channel in ten seconds, we're in good shape.", "8:48 AM"),
                new MessageResponse("p-5", "Priya", "Let's save anything more open-ended for later. Predictable outputs are better for a live walkthrough.", "8:53 AM")
        )));

        messagesByChannel.put("ai-design", new ArrayList<>(List.of(
                new MessageResponse("a-1", "Lena", "I want the Thursday demo to open on a channel that already looks active, not a blank state.", "10:03 AM"),
                new MessageResponse("a-2", "Marco", "Then the center pane should show a realistic planning conversation with enough detail for a good recap.", "10:07 AM"),
                new MessageResponse("a-3", "Lena", "Right. The AI panel needs to explain the conversation quickly: summary, action points, and decisions.", "10:12 AM"),
                new MessageResponse("a-4", "Ava", "Let's keep those actions on-demand. Clicking a button in the demo is easier to narrate than background automation.", "10:16 AM"),
                new MessageResponse("a-5", "Marco", "For scope, I think we should show only one polished main screen: left channels, center chat, right assistant panel.", "10:21 AM"),
                new MessageResponse("a-6", "Lena", "Agreed. We don't need threads, settings, or permissions for Thursday. We just need the screen to feel complete.", "10:25 AM"),
                new MessageResponse("a-7", "Ava", "I'll tighten the copy so the messages naturally imply owners and next steps. That should make action point extraction feel real.", "10:31 AM"),
                new MessageResponse("a-8", "Marco", "Can we make sure the seeded discussion mentions the actual demo flow? I want the recap to reinforce what we're about to show live.", "10:37 AM"),
                new MessageResponse("a-9", "Lena", "Yes. Let's explicitly mention channel switching, message send, and the AI recap buttons in the conversation.", "10:41 AM"),
                new MessageResponse("a-10", "Ava", "Decision-wise, I think we've already landed on in-memory data, deterministic AI responses, and no extra infrastructure before Thursday.", "10:48 AM"),
                new MessageResponse("a-11", "Marco", "Action items from my side: polish the panel cards, improve spacing in the chat, and make the loading state feel intentional.", "10:53 AM"),
                new MessageResponse("a-12", "Lena", "Great. If we can demo a believable project planning thread and the assistant turns it into useful output, that's a strong story.", "11:00 AM")
        )));

        messagesByChannel.put("backend", new ArrayList<>(List.of(
                new MessageResponse("b-1", "Chris", "I don't want any Thursday surprises from infra. Let's keep the chat-service fully in memory.", "1:05 PM"),
                new MessageResponse("b-2", "Nina", "Agreed. The frontend only needs channels, messages, post message, and three AI analysis endpoints.", "1:09 PM"),
                new MessageResponse("b-3", "Chris", "The AI responses can be deterministic as long as they sound specific to each conversation.", "1:13 PM"),
                new MessageResponse("b-4", "Nina", "Exactly. Clean structured payloads are more valuable than pretending we have a full model pipeline.", "1:18 PM"),
                new MessageResponse("b-5", "Chris", "Let's spend the remaining time on stability, seeded content, and making the API easy to demo locally.", "1:23 PM")
        )));

        messagesByChannel.put("meeting-notes", new ArrayList<>(List.of(
                new MessageResponse("m-1", "Nora", "Recap from today's sync: the Thursday walkthrough will focus on one polished channel experience.", "2:00 PM"),
                new MessageResponse("m-2", "Nora", "Confirmed demo sequence: switch into a seeded channel, review the conversation, send one new message, then run AI analysis.", "2:04 PM"),
                new MessageResponse("m-3", "Nora", "Decisions recorded: in-memory storage only, deterministic assistant responses, and no auth or extra infrastructure.", "2:07 PM"),
                new MessageResponse("m-4", "Nora", "Owners: Ava will polish the AI panel presentation, Marco will refine the conversation copy, Chris will keep the API stable.", "2:11 PM"),
                new MessageResponse("m-5", "Nora", "Success criteria: the app should feel calm, readable, and reliable enough to drive live without explaining caveats.", "2:15 PM")
        )));
    }
}
