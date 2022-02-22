package tasktree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.QuarkusApplication;
import tasktree.spi.Tasks;

import javax.inject.Inject;

public class TaskTree implements QuarkusApplication{
    private static final Logger log = LoggerFactory.getLogger(TaskTree.class);

    @Inject
    Tasks probes;

    @Override
    public int run(String... args){
        log.info("🐓 TaskTree");
        probes.run(args);
        return 0;
    }

}