package tasktree.help;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tasktree.BaseTask;
import tasktree.ReadTask;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

@Named("help")
@Dependent
public class HelpTask extends ReadTask {
    @Inject
    Logger log;

    @Override
    public void run() {
        log.info("Don't Panic!");
    }
}
