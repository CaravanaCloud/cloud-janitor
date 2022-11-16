package cj;

import java.util.List;

public class ConfigurationNotFoundException extends RuntimeException {
    private final List<InputConfig> missingInputs;

    public ConfigurationNotFoundException(List<InputConfig> missingInputs) {
        this.missingInputs = missingInputs;
    }

    public List<InputConfig> getMissingInputs() {
        return missingInputs;
    }

    @Override
    public String getMessage() {
        StringBuilder msg = new StringBuilder();
        for (var input: missingInputs) {
            msg.append("Missing input, try setting ");
            msg.append(input.configKey() + " (yaml) or ");
            msg.append(input.getEnvVarName()+ " (env)");
            msg.append("\n");
        }
        return msg.toString();
    }
}
