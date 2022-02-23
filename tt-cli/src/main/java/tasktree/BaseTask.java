package tasktree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tasktree.spi.Task;

public abstract class   BaseTask implements Task {
    static final Logger log = LoggerFactory.getLogger(BaseTask.class);
    protected Configuration config;

    public BaseTask(){}

    public BaseTask(Configuration config){
        this.config = config;
    }

    public void setConfig(Configuration config) {
        this.config = config;
    }

    public void addTask(Task task) {
        config.getTasks().addTask(task);
    }

    private static final int DEFAULT_RETRIES = 3;
    int retries = DEFAULT_RETRIES;
    @Override
    public int getRetries() {
        return retries;
    }

    @Override
    public void retried() {
        retries--;
    }

    @Override
    public String toString() {
        return getName();
    }

    protected Configuration getConfig(){
        return config;
    }

}
