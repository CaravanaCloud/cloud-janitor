package tasktree;

import tasktree.aws.AWSTask;
import tasktree.spi.Task;

public abstract class   BaseTask implements Task {
    protected Configuration config;

    public BaseTask(){}

    public BaseTask(Configuration config){
        this.config = config;
    }

    public void setConfig(Configuration config) {
        this.config = config;
    }

    public void push(Task task) {
        config.getTasks().push(task);
    }

    @Override
    public String toString() {
        return getName();
    }
}
