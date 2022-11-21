package cj.qute;

import javax.enterprise.context.Dependent;
import javax.inject.Named;

import cj.BaseTask;

@Dependent
@Named("qute-test")
public class QuteTest extends BaseTask {
    @Override
    public void apply() {
        info("Testing Qute");
        debug("Testing Qute");
        System.out.println("Testing Qute");
    }
}
