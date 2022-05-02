package cloudjanitor.simple;

import cloudjanitor.BaseTask;

import javax.enterprise.context.Dependent;

@Dependent
public class HelloTask extends BaseTask {
    @Override
    public void runSafe() {
        success("message", "hello world");
    }

    @Override
    public boolean isWrite() {
        return false;
    }
}
