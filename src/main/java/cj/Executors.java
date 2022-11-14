package cj;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.util.concurrent.ExecutorService;

@ApplicationScoped
public class Executors {
    @Produces
    public ExecutorService newExecutor() {
        return java.util.concurrent.Executors.newWorkStealingPool();
    }
}
