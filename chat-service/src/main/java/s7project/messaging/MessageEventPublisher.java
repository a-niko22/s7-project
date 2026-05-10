package s7project.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class MessageEventPublisher {
    static final String TOPIC = "chat.messages.v1";

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageEventPublisher.class);

    private final KafkaTemplate<String, MessageCreatedEvent> kafkaTemplate;

    public MessageEventPublisher(KafkaTemplate<String, MessageCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(MessageCreatedEvent event) {
        kafkaTemplate.send(TOPIC, event.channelId(), event)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        LOGGER.error("Failed to publish MessageCreated event {} for message {}",
                                event.eventId(), event.messageId(), exception);
                        return;
                    }

                    LOGGER.info("Published MessageCreated event {} for message {} to {}",
                            event.eventId(), event.messageId(), TOPIC);
                });
    }
}
