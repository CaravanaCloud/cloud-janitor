package cj;


import java.util.List;

public record SimpleTaskConfiguration(
        String name,
        String description,
        String maturityLevel,
        List<InputConfig> inputs)
        implements TaskConfiguration {

}
