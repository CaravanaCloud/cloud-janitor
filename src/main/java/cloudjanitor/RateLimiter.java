package cloudjanitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cloudjanitor.spi.Task;

import javax.enterprise.context.Dependent;

@Dependent
public class RateLimiter {
    static final Logger log = LoggerFactory.getLogger(RateLimiter.class);
    public void waitAfterTask(Task task) {
        var waitAfterRun = task.getWaitAfterRun();
        if (waitAfterRun.isPresent()){
            try {
                var sleep = waitAfterRun.get();
                log.debug("Waiting {} after {}",sleep,task);
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }

}