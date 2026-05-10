package s7project.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import s7project.model.AiInsightResponse;
import s7project.model.AiRequest;
import s7project.service.AiInsightService;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    private final AiInsightService aiInsightService;

    public AiController(AiInsightService aiInsightService) {
        this.aiInsightService = aiInsightService;
    }

    @PostMapping("/summary")
    public AiInsightResponse summary(@RequestBody AiRequest request) {
        return aiInsightService.generateSummary(request);
    }

    @PostMapping("/action-points")
    public AiInsightResponse actionPoints(@RequestBody AiRequest request) {
        return aiInsightService.generateActionPoints(request);
    }

    @PostMapping("/decisions")
    public AiInsightResponse decisions(@RequestBody AiRequest request) {
        return aiInsightService.generateDecisions(request);
    }
}
