package tasktree;

import org.slf4j.LoggerFactory;

import javax.enterprise.context.Dependent;
import javax.inject.Named;

@Dependent
@Named("empty")
public class EmptyTask extends BaseTask {
    @Override
    public void runSafe() {
        LoggerFactory
                .getLogger(EmptyTask.class)
                .info("Empty task");
    }
}
