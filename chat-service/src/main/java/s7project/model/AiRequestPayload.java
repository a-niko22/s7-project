package s7project.model;

import java.util.List;

public record AiRequestPayload(
        String channelId,
        String channelName,
        List<AiMessagePayload> messages
) {
}
