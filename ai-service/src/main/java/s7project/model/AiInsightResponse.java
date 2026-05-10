package s7project.model;

import java.util.List;

public record AiInsightResponse(
        String title,
        String subtitle,
        String content,
        List<String> bullets,
        String footer
) {
}
