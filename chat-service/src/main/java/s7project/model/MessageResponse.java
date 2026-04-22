package s7project.model;

public record MessageResponse(
        String id,
        String author,
        String text,
        String time
) {
}
