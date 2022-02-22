package tasktree.help;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tasktree.BaseTask;
import tasktree.spi.BaseResult;
import tasktree.spi.Task;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

@Named("help")
@ApplicationScoped
public class HelpTask extends BaseTask {
    static final Logger log = LoggerFactory.getLogger(HelpTask.class);
    @Override
    public void run() {
        log.info("Help task");
    }
}
