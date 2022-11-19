package cj;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public record InputConfig(
        Input input,
        String configKey,
        Function<Configuration, Optional<?>> configFn,
        Supplier<?> defaultFn
) {
    public String getEnvVarName(){
        return toEnvVarName(getConfigKey());
    }

    private String getConfigKey() {
        if (configKey == null) {
            var inputName =  input != null ? input.toString() : "INPUT_NAME";
            return "cj.GROUP_NAME." + inputName;
        } else return configKey;
    }

    private static String toEnvVarName(String configKey) {
        return (""+configKey)
                .replaceAll("[^a-zA-Z0-9]","_")
                .toUpperCase();
    }

    public static void main(String[] args) {
        System.out.println(toEnvVarName("cj.ocp.clusterName"));
    }

    public Optional<?> applyConfigFn(Configuration configuration) {
        if (configFn != null)
            return configFn.apply(configuration);
        return Optional.empty();
    }

    public Object applyDefaultFn() {
        if (defaultFn != null)
            return defaultFn.get();
        return null;
    }
}
