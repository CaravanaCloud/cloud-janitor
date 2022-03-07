package tasktree.marvin;

import org.slf4j.Logger;
import tasktree.ReadTask;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

@Named("marvin")
@Dependent
public class MarvinTask extends ReadTask {
    @Inject
    Logger log;

    @Override
    public void run() {
        log.warn("Don't Panic!");
    }
}
