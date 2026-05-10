package s7project.service;

public enum AiInsightType {
    SUMMARY("summary", "Summary"),
    ACTION_POINTS("action-points", "Action Points"),
    DECISIONS("decisions", "Decisions");

    private final String apiPath;
    private final String displayName;

    AiInsightType(String apiPath, String displayName) {
        this.apiPath = apiPath;
        this.displayName = displayName;
    }

    public String apiPath() {
        return apiPath;
    }

    public String displayName() {
        return displayName;
    }
}
