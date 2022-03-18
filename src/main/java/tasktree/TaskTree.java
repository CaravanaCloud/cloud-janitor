package tasktree;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.QuarkusApplication;
import tasktree.spi.Tasks;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

public class TaskTree implements QuarkusApplication{
    private static final Logger log = LoggerFactory.getLogger(TaskTree.class);

    @Inject
    TaskManager tasks;

    @Override
    public int run(String... args){
        tasks.runAll(args);
        //tasks.runAll(args);
        //Quarkus.waitForExit();
        return 0;
    }


    void onStart(@Observes StartupEvent ev) {
        log.debug("The application is starting...");
        // Set the default synchronous HTTP client to UrlConnectionHttpClient
        System.setProperty("software.amazon.awssdk.http.service.impl",
                "software.amazon.awssdk.http.urlconnection.UrlConnectionSdkHttpService");
    }

    void onStop(@Observes ShutdownEvent ev) {
        log.debug("The application is stopping...");
    }
}