package tasktree;

import javax.enterprise.inject.Produces;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorProvider {
    @Produces
    public ExecutorService newExecutor() {
        return Executors.newWorkStealingPool();
    }
}
