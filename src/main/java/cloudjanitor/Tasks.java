package cloudjanitor;

import cloudjanitor.aws.AWSClients;
import cloudjanitor.reporting.Reporting;
import cloudjanitor.spi.Task;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ApplicationScoped
public class Tasks {
    static final LocalDateTime startTime = LocalDateTime.now();

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

    List<Task> history = new ArrayList<>();

    public void run(String[] args) {
        var taskName = config.taskName();
        var matches = lookupTasks(taskName);
        runAll(matches);
        report();
    }

    private void report() {
        try {
            reporting.report(this);
        }catch (Exception ex){
            ex.printStackTrace();
            log.error("Reporting failed", ex);
        }
    }

    private void runAll(List<Task> matches) {
        matches.forEach(this::submit);
    }

    private List<Task> lookupTasks(String taskName) {
        log.debug("Looking up task "+taskName);
        return bm.getBeans(taskName)
                .stream()
                .map(this::fromBean)
                .toList();
    }

    public Task submit(Task task) {
        var dependencies = task.getDependencies();
        dependencies.forEach(this::submit);
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
        history.add(task);
        if (task.isWrite()
                && config.dryRun()) {
            log.trace("Dry run: {}", task);
        } else {
            try {
                task.setStartTime(LocalDateTime.now());
                task.apply();
                log.trace("Executed {} ({})",
                        task.toString(),
                        task.isWrite() ? "W" : "R");
                //TODO: General waiter
                if (task.isWrite()) {
                    rateLimiter.waitAfterTask(task);
                }
            } catch (Exception e) {
                task.getErrors().put(Errors.Message, e.getMessage());
                log.error("Error executing {}: {}", task.toString(), e.getMessage());
            }
        }
        task.setEndTime(LocalDateTime.now());
    }
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }
    public String getStartTimeFmt() {
        return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(startTime);
    }

    public Configuration getConfig() {
        return config;
    }

    String[] args;


    @Inject
    AWSClients aws;


    public void init(String[] args) {
        log.info("Configuration: {}", config);
        log.info("Args: {}", String.join(",", args));
        if(!config.dryRun()){
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String executionId;

    public synchronized String getExecutionId() {
        if (executionId == null){
            var sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
            executionId = "cj-"+sdf.format(new Date());
        }
        return executionId;
    }


}
