package s7project.model;

import java.util.List;

public record AiRequest(
        String channelId,
        String channelName,
        List<AiMessageRequest> messages
) {
}
