package cloudjanitor;

import cloudjanitor.reporting.Reporting;
import cloudjanitor.spi.Task;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

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

    @Inject
    Reporting reporting;

    public void run(String[] args) {
        var taskName = config.getTaskName();
        var matches = lookupTasks(taskName);
        runAll(matches);
        report(matches);
    }

    private void report(List<Task> matches) {
        try {
            reporting.report(matches);
        }catch (Exception ex){
            ex.printStackTrace();
            log.error("Reporting failed", ex);
        }
    }

    private void runAll(List<Task> matches) {
        matches.forEach(this::runTask);
    }

    private List<Task> lookupTasks(String taskName) {
        log.debug("Looking up task "+taskName);
        return bm.getBeans(taskName)
                .stream()
                .map(this::fromBean)
                .toList();
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
