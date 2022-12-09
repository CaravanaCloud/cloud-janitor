package cj;

import java.util.List;

public class ConfigurationNotFoundException extends RuntimeException {
    private final List<InputConfig> missingInputs;

    public ConfigurationNotFoundException(List<InputConfig> missingInputs) {
        this.missingInputs = missingInputs;
    }

    @Override
    public String getMessage() {
        var msg = new StringBuilder();
        for (var input: missingInputs) {
            var configKey = input.configKey();
            msg.append("Missing input, try setting ");
            msg.append(configKey);
            msg.append(" (yaml) or ");
            msg.append("\n");
        }
        @SuppressWarnings("redundant")
        var result = msg.toString();
        return result;
    }
}
