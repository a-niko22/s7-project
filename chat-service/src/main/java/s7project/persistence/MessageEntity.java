package s7project.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "messages")
public class MessageEntity {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "channel_id", nullable = false)
    private ChannelEntity channel;

    private String authorId;

    @Column(nullable = false)
    private String authorName;

    @Column(nullable = false, length = 4000)
    private String text;

    @Column(nullable = false)
    private Instant createdAt;

    protected MessageEntity() {
    }

    public MessageEntity(UUID id, ChannelEntity channel, String authorId, String authorName, String text, Instant createdAt) {
        this.id = id;
        this.channel = channel;
        this.authorId = authorId;
        this.authorName = authorName;
        this.text = text;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public ChannelEntity getChannel() {
        return channel;
    }

    public String getAuthorId() {
        return authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getText() {
        return text;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
