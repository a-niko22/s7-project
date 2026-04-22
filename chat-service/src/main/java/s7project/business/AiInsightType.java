package s7project.business;

public enum AiInsightType {
    SUMMARY("summary"),
    ACTION_POINTS("action-points"),
    DECISIONS("decisions");

    private final String path;

    AiInsightType(String path) {
        this.path = path;
    }

    public String path() {
        return path;
    }
}
