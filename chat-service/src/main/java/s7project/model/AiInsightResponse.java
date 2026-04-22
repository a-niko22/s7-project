package s7project.model;

import java.util.List;

public record AiInsightResponse(
        String title,
        String content,
        List<String> bullets
) {
}
