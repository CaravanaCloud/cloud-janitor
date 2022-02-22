package tasktree;

import org.slf4j.LoggerFactory;
import tasktree.spi.Task;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EmptyTask extends BaseTask {
    @Override
    public void run() {
        LoggerFactory
                .getLogger(EmptyTask.class)
                .info("Empty task");
    }
}
