package s7project.persistence;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ChatMapperTests {
    private final ChatMapper chatMapper = new ChatMapper();

    @Test
    void mapsChannelEntityToResponseShape() {
        ChannelEntity channel = new ChannelEntity("general", "general", "Team channel", 0);

        var response = chatMapper.toResponse(channel);

        assertThat(response.id()).isEqualTo("general");
        assertThat(response.name()).isEqualTo("general");
        assertThat(response.description()).isEqualTo("Team channel");
    }

    @Test
    void mapsMessageEntityToResponseShape() {
        UUID messageId = UUID.randomUUID();
        ChannelEntity channel = new ChannelEntity("general", "general", "Team channel", 0);
        MessageEntity message = new MessageEntity(
                messageId,
                channel,
                null,
                "You",
                "Hello",
                Instant.parse("2026-05-10T10:15:00Z")
        );

        var response = chatMapper.toResponse(message);

        assertThat(response.id()).isEqualTo(messageId.toString());
        assertThat(response.author()).isEqualTo("You");
        assertThat(response.text()).isEqualTo("Hello");
        assertThat(response.time()).isNotBlank();
    }
}
