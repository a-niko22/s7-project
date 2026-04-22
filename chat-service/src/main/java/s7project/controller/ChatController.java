package s7project.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import s7project.business.ChatService;
import s7project.model.AiInsightResponse;
import s7project.model.ChannelResponse;
import s7project.model.MessageRequest;
import s7project.model.MessageResponse;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/channels")
    public List<ChannelResponse> getChannels() {
        return chatService.getChannels();
    }

    @GetMapping("/channels/{channelId}/messages")
    public List<MessageResponse> getMessages(@PathVariable String channelId) {
        return chatService.getMessages(channelId);
    }

    @PostMapping("/channels/{channelId}/messages")
    public MessageResponse createMessage(@PathVariable String channelId, @RequestBody MessageRequest request) {
        return chatService.createMessage(channelId, request);
    }

    @PostMapping("/channels/{channelId}/ai/summary")
    public AiInsightResponse summarize(@PathVariable String channelId) {
        return chatService.getSummary(channelId);
    }

    @PostMapping("/channels/{channelId}/ai/action-points")
    public AiInsightResponse actionPoints(@PathVariable String channelId) {
        return chatService.getActionPoints(channelId);
    }

    @PostMapping("/channels/{channelId}/ai/decisions")
    public AiInsightResponse decisions(@PathVariable String channelId) {
        return chatService.getDecisions(channelId);
    }
}
