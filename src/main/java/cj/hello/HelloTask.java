package cj.hello;

import org.slf4j.Logger;
import cj.ReadTask;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

import static cj.Input.Local.message;

@Named("hello")
@Dependent
public class HelloTask extends ReadTask {
    static final String initialMsg = "Hello, thank you for calling Cloud Janitor.";
    @Inject
    Logger log;

    @Override
    public void apply() {
        var msg = inputString(message);
        log.info(msg.orElse(initialMsg));
    }
}
