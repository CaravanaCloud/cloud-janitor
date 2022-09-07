package cloudjanitor;

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

import static cloudjanitor.Errors.Type.Message;

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
        log.trace("Tasks.run()",args);
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
        var tasks = bm.getBeans(taskName)
                .stream()
                .map(this::fromBean)
                .toList();
        log.info("Loaded {} tasks with name {} ", tasks.size(), taskName);
        return tasks;
    }

    //TODO: Consider cycles
    public Task submit(Task task) {
        task.init();
        var thisInputs = task.getInputs();
        var dependencies = task.getDependencies();
        dependencies.forEach(d -> {
            d.getInputs().putAll(thisInputs);
            submit(d);
        });
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
            throw new IllegalArgumentException("Bean is not a task");
        }
    }

    //TODO: Consider retries
    //TODO: Consider thread synchronization
    public void runSingle(Task task) {
        history.add(task);
        if (task.isWrite()
                && config.dryRun()) {
            log.warn("[dry-run] Rejecting write task: {}", task);
        } else {
            try {
                task.setStartTime(LocalDateTime.now());
                task.apply();
                log.trace("Executed {} ({})",
                        task,
                        task.isWrite() ? "W" : "R");
                //TODO: General waiter
                if (task.isWrite()) {
                    rateLimiter.waitAfterTask(task);
                }
            } catch (Exception e) {
                task.getErrors().put(Message, e.getMessage());
                log.error("Error executing {}: {}", task, e.getMessage());
                e.printStackTrace();
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
