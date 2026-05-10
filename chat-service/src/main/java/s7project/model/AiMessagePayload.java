package s7project.model;

public record AiMessagePayload(
        String author,
        String text,
        String time
) {
}
