package cj.hello;

import cj.BaseTask;
import cj.Output;
import org.slf4j.Logger;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

import static cj.LocalInput.message;

@Named("hello")
@Dependent
public class HelloTask extends BaseTask {
    static final String initialMsg = "Hello World!";
    @Inject
    Logger log;

    @Override
    public void apply() {
        var msg = inputString(message).orElse(initialMsg);
        log.info(msg);
        success(Output.sample.Message, msg);
    }
}
