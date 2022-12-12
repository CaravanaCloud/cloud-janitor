package cj;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

//TODO: Map separately from input config
public record InputFunctions(
        Input input,
        String description,
        String configKey,
        Function<Configuration, Optional<?>> configFn,
        Supplier<?> defaultFn,
        String defaultDescription,
        List<String> allowedValues,
        boolean enrichBypass) {

    private String getConfigKey() {
        if (configKey == null) {
            var inputName =  input != null ? input.toString() : "INPUT_NAME";
            return "cj.GROUP_NAME." + inputName;
        } else return configKey;
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

    public static InputFunctions of(Input key,
                                    String description,
                                    String configKey,
                                    Function<Configuration, Optional<?>> configFn,
                                    Supplier<?> defaultFn,
                                    String defaultDescription,
                                    Object[] allowedValues, boolean enrichBypass) {
        var allowedStrings = (List<String>) null;
        if(allowedValues != null){
            allowedStrings = Arrays.stream(allowedValues).map(Object::toString).toList();
        }
        return new InputFunctions(key,
            description, 
            configKey, 
            configFn, 
            defaultFn, 
            defaultDescription, 
            allowedStrings, enrichBypass);
    }
}
