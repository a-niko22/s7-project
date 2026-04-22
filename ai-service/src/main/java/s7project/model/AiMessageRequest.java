package s7project.model;

public record AiMessageRequest(
        String author,
        String text,
        String time
) {
}
