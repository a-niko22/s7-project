package s7project.messaging;

import org.springframework.stereotype.Component;
import s7project.persistence.MessageEntity;

import java.util.UUID;

@Component
public class MessageCreatedEventFactory {
    public MessageCreatedEvent from(MessageEntity message) {
        return new MessageCreatedEvent(
                UUID.randomUUID(),
                "MessageCreated",
                message.getId(),
                message.getChannel().getId(),
                message.getAuthorId(),
                message.getAuthorName(),
                message.getText(),
                message.getCreatedAt()
        );
    }
}
