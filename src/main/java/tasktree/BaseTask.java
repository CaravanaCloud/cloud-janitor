package tasktree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tasktree.spi.Task;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Dependent
public abstract class BaseTask implements Task {
    static final Logger log = LoggerFactory.getLogger(BaseTask.class);

    @Inject
    Configuration config;

    Result result;

    public BaseTask(){}

    public BaseTask(Configuration config){
        this.config = config;
    }

    public void setConfig(Configuration config) {
        this.config = config;
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

    @Override
    public Configuration getConfig(){
        return config;
    }

    Map<String, String> properties = new HashMap<>();

    @Override
    public String get(String key) {
        return properties.get(key);
    }

    @Override
    public String set(String key, String value) {
        return properties.put(key, value);
    }

    @Override
    public String toString() {
        return asString(getSimpleName());
    }

    public String asString(String name,
                           String... extras) {
        StringBuffer sb = new StringBuffer();
        sb.append(name);
        sb.append(" ");
        var _extras = Stream.of(extras).map(e -> "(%s)".formatted(e)).toList();
        var __extras = String.join(" ",_extras) ;
        sb.append(__extras);
        sb.append(" *"+getRetries());
        return sb.toString();
    }

    @Override
    public List<Task> getSubtasks(){
        return List.of();
    }

    @Override
    public void runSafe(){
        config.runTask(this);
    }

    @Override
    public Result getResult() {
        return result;
    }

    @Override
    public void setResult(Result result) {
        this.result = result;
    }

}
