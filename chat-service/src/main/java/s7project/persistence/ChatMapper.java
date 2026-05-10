package s7project.persistence;

import org.springframework.stereotype.Component;
import s7project.model.ChannelResponse;
import s7project.model.MessageResponse;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
public class ChatMapper {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter
            .ofPattern("h:mm a", Locale.ENGLISH)
            .withZone(ZoneId.systemDefault());

    public ChannelResponse toResponse(ChannelEntity channel) {
        return new ChannelResponse(channel.getId(), channel.getName(), channel.getDescription());
    }

    public MessageResponse toResponse(MessageEntity message) {
        return new MessageResponse(
                message.getId().toString(),
                message.getAuthorName(),
                message.getText(),
                TIME_FORMATTER.format(message.getCreatedAt())
        );
    }
}
