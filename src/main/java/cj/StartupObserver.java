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

    public void putConfig(Input input,
                               String description,
                               String configKey,
                               Function<Configuration, Optional<?>> configFn,
                               Supplier<?> defaultFn,
                               String defaultDescription){
        putConfig(input, description, configKey, configFn, defaultFn, defaultDescription, new Object[]{});
    }
    public void putConfig(Input input,
                               String description,
                               String configKey,
                               Function<Configuration, Optional<?>> configFn,
                               Supplier<?> defaultFn,
                               String defaultDescription,
                               Object[] allowedValues){
        inputs.putConfig(input, 
            description, 
            configKey, 
            configFn, 
            defaultFn,
            defaultDescription,
            allowedValues);
    }
    
    public void putConfig(Input input,
                               String configKey,
                               Function<Configuration, Optional<?>> configFn,
                               Supplier<?> defaultFn){
        inputs.putConfig(input, "", configKey, configFn, defaultFn, "" , new Object[]{});
    }

    @SuppressWarnings("unused")
    protected void onStartupEvent(@Observes StartupEvent ev){
        onStart();
    }

    protected void onStart() {
        debug("Startup observer completed.");
    }
}
