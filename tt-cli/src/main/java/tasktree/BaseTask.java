package tasktree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tasktree.spi.Task;

import javax.enterprise.context.Dependent;

@Dependent
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

    protected Configuration getConfig(){
        return config;
    }

    protected String getRWMark(){
        return isWrite() ? "W" : "R";
    }


    @Override
    public String toString() {
        return toString(null);
    }

    public String toString(String resourceType, String... obs) {
        return "%s [%s] [%s] Retries[%d]".formatted(
                resourceType != null ? resourceType : getSimpleName(),
                String.join(",", obs),
                getRWMark(),
                getRetries()
        );
    }


}
