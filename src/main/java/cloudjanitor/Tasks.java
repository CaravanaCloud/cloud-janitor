package cloudjanitor;

import cloudjanitor.spi.Task;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import java.time.LocalDateTime;

@ApplicationScoped
public class Tasks {
    @Inject
    Logger log;

    @Inject
    BeanManager bm;

    @Inject
    Configuration config;

    @Inject
    RateLimiter rateLimiter;


    public void run(String[] args) {
        var taskName = config.getTaskName();
        runByName(taskName);
    }

    private void runByName(String taskName) {
        log.debug("Looking up task "+taskName);
        bm.getBeans(taskName)
                .stream()
                .map(this::fromBean)
                .forEach(this::runTask);
    }

    public Task runTask(Task task) {
        var dependencies = task.getDependencies();
        dependencies.forEach(this::runTask);
        runSingle(task);
        return task;
    }

    private Task fromBean(Bean<?> bean) {
        var ctx = bm.createCreationalContext(bean);
        var ref = bm.getReference(bean, bean.getBeanClass(), ctx);
        if (ref instanceof Task task) {
            return task;
        }else{
            log.error("Bean {} is not a Task", bean);
            return null;
        }
    }

    //TODO: Consider retries
    public void runSingle(Task task) {
        if (task.isWrite()
                && config.isDryRun()) {
            log.trace("Dry run: {}", task);
        } else {
            try {
                task.setStartTime(LocalDateTime.now());
                task.runSafe();
                log.trace("Executed {} ({})",
                        task.toString(),
                        task.isWrite() ? "W" : "R");
                //TODO: General waiter
                if (task.isWrite()) {
                    rateLimiter.waitAfterTask(task);
                }
            } catch (Exception e) {
                task.getErrors().put("exception", e.getMessage());
                log.error("Error executing {}: {}", task.toString(), e.getMessage());
            }
        }
        task.setEndTime(LocalDateTime.now());
    }

}
