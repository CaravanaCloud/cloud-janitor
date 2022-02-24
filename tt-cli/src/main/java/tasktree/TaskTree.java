package tasktree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.QuarkusApplication;
import tasktree.spi.Tasks;

import javax.inject.Inject;

public class TaskTree implements QuarkusApplication{
    private static final Logger log = LoggerFactory.getLogger(TaskTree.class);

    @Inject
    Tasks tasks;

    @Override
    public int run(String... args){
        tasks.runAll(args);
        //Quarkus.waitForExit();
        return 0;
    }

    /*
    void onStart(@Observes StartupEvent ev) {
        log.info("The application is starting...");
    }

    void onStop(@Observes ShutdownEvent ev) {
        log.info("The application is stopping...");
        System.exit(0);
    }
    */
}