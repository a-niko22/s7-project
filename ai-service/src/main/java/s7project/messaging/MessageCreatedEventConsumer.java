package s7project.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MessageCreatedEventConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageCreatedEventConsumer.class);

    @KafkaListener(topics = "chat.messages.v1", groupId = "ai-service-message-consumer")
    public void onMessageCreated(MessageCreatedEvent event) {
        LOGGER.info("Received {} event {} for message {} in channel {} from {}",
                event.type(),
                event.eventId(),
                event.messageId(),
                event.channelId(),
                event.authorName());
    }
}
