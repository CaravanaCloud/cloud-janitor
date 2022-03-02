package tasktree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tasktree.spi.Task;

import javax.enterprise.context.Dependent;

@Dependent
public abstract class BaseTask implements Task {
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

    private static final int DEFAULT_RETRIES = 5;
    int retries = DEFAULT_RETRIES;
    @Override
    public int getRetries() {
        return retries;
    }

    @Override
    public void retried() {
        retries--;
    }

    protected Configuration getConfig(){
        return config;
    }


    @Override
    public String toString() {
        return asString(null,null);
    }

    public String asString(String taskName,
                           String resourceType,
                           String... taskInfo) {
        StringBuffer sb = new StringBuffer();
        sb.append(getName());
        if (resourceType != null) {
            sb.append(" (");
            sb.append(resourceType);
            sb.append(")");
        }
        if (taskInfo != null) {
            sb.append("(");
            sb.append(String.join(",", taskInfo));
            sb.append(")");
        }
        sb.append(" *"+getRetries());
        return sb.toString();
    }


}
