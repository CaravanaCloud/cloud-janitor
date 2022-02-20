package tasktree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.QuarkusApplication;
import tasktree.spi.Probe;
import tasktree.spi.Probes;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

public class TaskTree implements QuarkusApplication{
    private static final Logger log = LoggerFactory.getLogger(TaskTree.class);

    @Inject
    Probes probes;

    @Override
    public int run(String... args){
        log.info("🐓 TaskTree");
        probes.run(args);
        return 0;
    }

}