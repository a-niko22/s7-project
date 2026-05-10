package s7project.persistence;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Component
public class ChatDataSeeder implements CommandLineRunner {
    private final ChannelJpaRepository channelJpaRepository;
    private final MessageJpaRepository messageJpaRepository;

    public ChatDataSeeder(ChannelJpaRepository channelJpaRepository, MessageJpaRepository messageJpaRepository) {
        this.channelJpaRepository = channelJpaRepository;
        this.messageJpaRepository = messageJpaRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (channelJpaRepository.count() > 0 || messageJpaRepository.count() > 0) {
            return;
        }

        List<ChannelEntity> channels = channelJpaRepository.saveAll(List.of(
                new ChannelEntity("general", "general", "Team-wide alignment for the Thursday walkthrough", 0),
                new ChannelEntity("project-ideas", "project-ideas", "How the assistant should feel in the demo", 1),
                new ChannelEntity("ai-design", "ai-design", "Main planning thread for the demo experience", 2),
                new ChannelEntity("backend", "backend", "Delivery plan for the demo API", 3),
                new ChannelEntity("meeting-notes", "meeting-notes", "Recaps, owners, and agreed decisions", 4)
        ));

        messageJpaRepository.saveAll(List.of(
                message(channels.get(0), "Mia", "For Thursday, I want us to demo one screen really well instead of trying to show a whole collaboration suite.", 9, 2),
                message(channels.get(0), "Alex", "Agreed. The strongest flow is: open a channel, scan the discussion, send a message, then use AI to summarize what changed.", 9, 8),
                message(channels.get(0), "Jordan", "That means the seeded conversation quality matters a lot. The AI output will feel fake if the messages are too generic.", 9, 12),
                message(channels.get(0), "Mia", "Let's make the content sound like a real project team deciding what to show, who owns what, and what we intentionally cut.", 9, 18),
                message(channels.get(0), "Alex", "Perfect. If the UI looks clean and the recap cards feel useful, that's enough to land the story.", 9, 22),
                message(channels.get(1), "Dina", "The AI panel should feel like a project coordinator, not like a chatbot stuck on the side of the screen.", 8, 35),
                message(channels.get(1), "Sam", "Yes. For the demo, every action should produce something structured and immediately readable.", 8, 39),
                message(channels.get(1), "Dina", "Summary should answer 'what happened', action points should answer 'what happens next', and decisions should answer 'what is already settled'.", 8, 44),
                message(channels.get(1), "Sam", "Exactly. If a viewer can read the panel from a distance and understand the channel in ten seconds, we're in good shape.", 8, 48),
                message(channels.get(1), "Priya", "Let's save anything more open-ended for later. Predictable outputs are better for a live walkthrough.", 8, 53),
                message(channels.get(2), "Lena", "I want the Thursday demo to open on a channel that already looks active, not a blank state.", 10, 3),
                message(channels.get(2), "Marco", "Then the center pane should show a realistic planning conversation with enough detail for a good recap.", 10, 7),
                message(channels.get(2), "Lena", "Right. The AI panel needs to explain the conversation quickly: summary, action points, and decisions.", 10, 12),
                message(channels.get(2), "Ava", "Let's keep those actions on-demand. Clicking a button in the demo is easier to narrate than background automation.", 10, 16),
                message(channels.get(2), "Marco", "For scope, I think we should show only one polished main screen: left channels, center chat, right assistant panel.", 10, 21),
                message(channels.get(2), "Lena", "Agreed. We don't need threads, settings, or permissions for Thursday. We just need the screen to feel complete.", 10, 25),
                message(channels.get(2), "Ava", "I'll tighten the copy so the messages naturally imply owners and next steps. That should make action point extraction feel real.", 10, 31),
                message(channels.get(2), "Marco", "Can we make sure the seeded discussion mentions the actual demo flow? I want the recap to reinforce what we're about to show live.", 10, 37),
                message(channels.get(2), "Lena", "Yes. Let's explicitly mention channel switching, message send, and the AI recap buttons in the conversation.", 10, 41),
                message(channels.get(2), "Ava", "Decision-wise, we've landed on Postgres for persisted chat history and Kafka events for async follow-up work.", 10, 48),
                message(channels.get(2), "Marco", "Action items from my side: polish the panel cards, confirm Docker Compose starts cleanly, and make the loading state feel intentional.", 10, 53),
                message(channels.get(2), "Lena", "Great. If we can demo a believable project planning thread and the assistant turns it into useful output, that's a strong story.", 11, 0),
                message(channels.get(3), "Chris", "I don't want any Thursday surprises from infra. Let's keep Postgres as the source of truth and Kafka limited to message events.", 13, 5),
                message(channels.get(3), "Nina", "Agreed. The frontend only needs channels, messages, post message, and three AI analysis endpoints.", 13, 9),
                message(channels.get(3), "Chris", "The AI responses can be deterministic as long as they sound specific to each conversation.", 13, 13),
                message(channels.get(3), "Nina", "Exactly. Clean REST payloads and clear Kafka events are more valuable than expanding the scope into a full model pipeline.", 13, 18),
                message(channels.get(3), "Chris", "Let's spend the remaining time on stable Compose startup, seeded content, and making the API easy to demo locally.", 13, 23),
                message(channels.get(4), "Nora", "Recap from today's sync: the Thursday walkthrough will focus on one polished channel experience.", 14, 0),
                message(channels.get(4), "Nora", "Confirmed demo sequence: switch into a seeded channel, review the conversation, send one new message, then run AI analysis.", 14, 4),
                message(channels.get(4), "Nora", "Decisions recorded: Postgres persistence, Kafka message-created events, deterministic assistant responses, and no auth expansion.", 14, 7),
                message(channels.get(4), "Nora", "Owners: Ava will polish the AI panel presentation, Marco will refine the conversation copy, Chris will keep Docker Compose stable.", 14, 11),
                message(channels.get(4), "Nora", "Success criteria: the app should feel calm, readable, and reliable enough to drive live without explaining caveats.", 14, 15)
        ));
    }

    private MessageEntity message(ChannelEntity channel, String authorName, String text, int hour, int minute) {
        return new MessageEntity(
                UUID.randomUUID(),
                channel,
                null,
                authorName,
                text,
                LocalDate.now().atTime(LocalTime.of(hour, minute)).atZone(ZoneId.systemDefault()).toInstant()
        );
    }
}
