package cj;

import java.util.List;

public class ConfigurationNotFoundException extends RuntimeException {
    private final List<String> varNames;

    public ConfigurationNotFoundException(List<String> varNames) {
        this.varNames = varNames;
    }

    public List<String> getVarName() {
        return varNames;
    }
}
