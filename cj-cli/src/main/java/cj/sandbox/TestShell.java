package cj.sandbox;

import cj.BaseTask;

import javax.enterprise.context.Dependent;
import javax.inject.Named;

@Dependent
@Named("test-shell")
public class TestShell extends BaseTask {
    public void apply() {
        var out1 = exec("echo hello1");
        var out2 = exec("logger -s hello2");

        out1.ifPresent(System.out::println);
        System.out.println(out2.orElse("no output"));
    }

}
