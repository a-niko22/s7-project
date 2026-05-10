package s7project.messaging;

import java.time.Instant;
import java.util.UUID;

public record MessageCreatedEvent(
        UUID eventId,
        String type,
        UUID messageId,
        String channelId,
        String authorId,
        String authorName,
        String text,
        Instant createdAt
) {
}
