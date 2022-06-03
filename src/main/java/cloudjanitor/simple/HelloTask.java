package cloudjanitor.simple;

import cloudjanitor.BaseTask;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
public class HelloTask extends BaseTask {
    @Inject
    Logger log;
    @Override
    public void runSafe() {
        String message = getConfig()
                .inputs()
                .getOrDefault("message", "hello world!");
        success("message", message);
    }

    @Override
    public boolean isWrite() {
        return false;
    }

}
