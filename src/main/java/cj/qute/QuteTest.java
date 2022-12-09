package cj.qute;

import cj.BaseTask;

import javax.enterprise.context.Dependent;
import javax.inject.Named;

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
