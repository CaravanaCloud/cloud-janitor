package cj.hello;

import cj.Output;
import cj.ReadTask;
import org.slf4j.Logger;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

import static cj.Input.local.message;

@Named("hello")
@Dependent
public class HelloTask extends ReadTask {
    static final String initialMsg = "Hello World!";
    @Inject
    Logger log;

    @Override
    public void apply() {
        var msg = inputString(message).orElse(initialMsg);
        log.info(msg);
        success(Output.Sample.Message, msg);
    }
}
