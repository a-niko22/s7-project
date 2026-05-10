package s7project.persistence;

import org.springframework.stereotype.Repository;
import s7project.model.ChannelResponse;
import s7project.model.MessageResponse;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ChatRepository {
    private final ChannelJpaRepository channelJpaRepository;
    private final MessageJpaRepository messageJpaRepository;
    private final ChatMapper chatMapper;

    public ChatRepository(
            ChannelJpaRepository channelJpaRepository,
            MessageJpaRepository messageJpaRepository,
            ChatMapper chatMapper
    ) {
        this.channelJpaRepository = channelJpaRepository;
        this.messageJpaRepository = messageJpaRepository;
        this.chatMapper = chatMapper;
    }

    public List<ChannelResponse> getChannels() {
        return channelJpaRepository.findAllByOrderBySortOrderAsc().stream()
                .map(chatMapper::toResponse)
                .toList();
    }

    public Optional<ChannelEntity> findChannel(String channelId) {
        return channelJpaRepository.findById(channelId);
    }

    public List<MessageResponse> getMessages(String channelId) {
        return messageJpaRepository.findByChannel_IdOrderByCreatedAtAsc(channelId).stream()
                .map(chatMapper::toResponse)
                .toList();
    }

    public MessageEntity addMessage(ChannelEntity channel, String authorId, String authorName, String text) {
        MessageEntity newMessage = new MessageEntity(
                UUID.randomUUID(),
                channel,
                authorId,
                authorName,
                text,
                Instant.now()
        );

        return messageJpaRepository.save(newMessage);
    }

    public MessageResponse toResponse(MessageEntity message) {
        return chatMapper.toResponse(message);
    }
}
