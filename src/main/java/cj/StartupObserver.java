package cj;

import io.quarkus.runtime.StartupEvent;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@Dependent
public abstract class StartupObserver implements  Logging{
    @Inject
    InputsMap inputs;

    @Inject
    Tasks tasks;

    @Inject
    Shell shell;

    @Inject
    Configuration config;

    protected Shell shell() {
        return shell;
    }

    protected Configuration config() {
        return config;
    }

    public void describeInput(Input input,
                              String description,
                              String configKey,
                              Function<CJConfiguration, Optional<?>> configFn,
                              Supplier<?> defaultFn,
                              String defaultDescription){
        describeInput(input, description, configKey, configFn, defaultFn, defaultDescription, new Object[]{}, false);
    }
    public void describeInput(Input input,
                              String description,
                              String configKey,
                              Function<CJConfiguration, Optional<?>> configFn,
                              Supplier<?> defaultFn,
                              String defaultDescription,
                              Object[] allowedValues){
        describeInput(input, description, configKey, configFn, defaultFn, defaultDescription, allowedValues, false);
    }
    public void describeInput(Input input,
                              String description,
                              String configKey,
                              Function<CJConfiguration, Optional<?>> configFn,
                              Supplier<?> defaultFn,
                              String defaultDescription,
                              Object[] allowedValues,
                              boolean enrichBypass){
        inputs.putConfig(input,
            description, 
            configKey, 
            configFn, 
            defaultFn,
            defaultDescription,
            allowedValues,
                enrichBypass);
    }
    
    public void describeInput(Input input,
                              String configKey,
                              Function<CJConfiguration, Optional<?>> configFn,
                              Supplier<?> defaultFn){
        describeInput(input, "", configKey, configFn, defaultFn, "" , new Object[]{}, false);
    }

    @SuppressWarnings("unused")
    protected void onStartupEvent(@Observes StartupEvent ev){
        onStart();
    }

    protected void onStart() {
        debug("Startup observer completed.");
    }

    public Tasks tasks() {
        return tasks;
    }
}
