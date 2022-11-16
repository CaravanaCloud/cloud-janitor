package cj;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public record InputConfig(
        Input input,
        String configKey,
        Function<Configuration, Optional<String>> configFn,
        Supplier<String> defaultFn
) {
    public String getEnvVarName(){
        return toEnvVarName(configKey);
    }

    private static String toEnvVarName(String configKey) {
        return (""+configKey)
                .replaceAll("[^a-zA-Z0-9]","_")
                .toUpperCase();
    }

    public static void main(String[] args) {
        System.out.println(toEnvVarName("cj.ocp.clusterName"));
    }

    public Optional<String> applyConfigFn(Configuration configuration) {
        if (configFn != null)
            return configFn.apply(configuration);
        return null;
    }

    public String applyDefaultFn() {
        if (defaultFn != null)
            return defaultFn.get();
        return null;
    }
}
