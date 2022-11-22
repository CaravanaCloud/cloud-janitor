package cj;


public record SimpleTaskConfiguration(
        String name,
        String description,
        String maturityLevel)
        implements TaskConfiguration {

    public static TaskConfiguration of(String taskName) {
        return new SimpleTaskConfiguration(taskName, null, null);
    }
}
