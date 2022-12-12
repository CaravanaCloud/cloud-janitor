package cj;

import java.util.List;
import java.util.Optional;

public record SimpleTaskConfiguration(
        String name,
        Optional<String> description,
        Optional<String> maturity,
        Optional<List<InputConfiguration>> inputs,
        Optional<List<String>> bypass
) implements TaskConfiguration {

    public static SimpleTaskConfiguration of(String taskName,
                                             String description,
                                             String maturity,
                                             List<InputConfiguration> inputs,
                                             List<String> bypass) {
        if (taskName == null) {
            return null;
        }
        return new SimpleTaskConfiguration(taskName,
                Optional.ofNullable(maturity),
                Optional.ofNullable(description),
                Optional.ofNullable(inputs),
                Optional.empty());
    }
}