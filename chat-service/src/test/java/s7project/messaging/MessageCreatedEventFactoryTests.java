package s7project.messaging;

import org.junit.jupiter.api.Test;
import s7project.persistence.ChannelEntity;
import s7project.persistence.MessageEntity;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MessageCreatedEventFactoryTests {
    private final MessageCreatedEventFactory factory = new MessageCreatedEventFactory();

    @Test
    void createsMessageCreatedEventFromSavedMessage() {
        UUID messageId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-05-10T10:15:00Z");
        ChannelEntity channel = new ChannelEntity("ai-design", "ai-design", "Design thread", 2);
        MessageEntity message = new MessageEntity(messageId, channel, "user-1", "Mia", "Hello", createdAt);

        MessageCreatedEvent event = factory.from(message);

        assertThat(event.eventId()).isNotNull();
        assertThat(event.type()).isEqualTo("MessageCreated");
        assertThat(event.messageId()).isEqualTo(messageId);
        assertThat(event.channelId()).isEqualTo("ai-design");
        assertThat(event.authorId()).isEqualTo("user-1");
        assertThat(event.authorName()).isEqualTo("Mia");
        assertThat(event.text()).isEqualTo("Hello");
        assertThat(event.createdAt()).isEqualTo(createdAt);
    }
}
