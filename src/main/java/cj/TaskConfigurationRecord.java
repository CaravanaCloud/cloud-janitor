package cj;

import java.util.List;
import java.util.Optional;

public record TaskConfigurationRecord(
        String name,
        Optional<String> description,
        Optional<String> maturity,
        Optional<List<InputConfiguration>> inputs,
        Optional<List<String>> bypass,
        Optional<TaskRepeat> repeat,
        Optional<InstallConfig> install,
        List<TemplateConfig> templates)
            implements TaskConfiguration {

    public static TaskConfigurationRecord of(String taskName,
                                             String description,
                                             String maturity,
                                             List<InputConfiguration> inputs,
                                             List<String> bypass,
                                             TaskRepeat repeat) {
        if (taskName == null) {
            return null;
        }
        return new TaskConfigurationRecord(taskName,
                Optional.ofNullable(maturity),
                Optional.ofNullable(description),
                Optional.ofNullable(inputs),
                Optional.empty(),
                Optional.ofNullable(repeat),
                Optional.empty(),
                List.of());
    }
}
