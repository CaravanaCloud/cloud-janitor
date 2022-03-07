package tasktree.spi;

import org.slf4j.Logger;
import tasktree.Configuration;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Stream;

@ApplicationScoped
public class Tasks {
    static final ServiceLoader<Task> loader = ServiceLoader.load(Task.class);

    @Inject
    Logger log;

    @Inject
    @Any
    Instance<Task> tasks;

    @Inject
    BeanManager bm;

    @Inject
    Configuration config;

    Deque<Task> readQueue = new LinkedList<>();
    Deque<Task> writeQueue = new LinkedList<>();

    public static Stream<Task> byProviders() {
        return byProviders(true).stream();
    }

    public static List<Task> byProviders(boolean refresh) {
        if (refresh) {
            loader.reload();
        }
        var result = new ArrayList<Task>();
        loader.iterator().forEachRemaining(result::add);
        return result;
    }


    public Stream<Task> stream() {
        var concat = Stream.concat(byProviders(), byCDI());
        return concat;
    }

    private Stream<Task> byCDI() {
        return tasks.stream();
    }

    public void runAll(String[] args) {
        config.init(args);
        var taskName = config.getTaskName();
        runByName(taskName);
    }

    private void runByName(String taskName) {
        bm.getBeans(taskName)
                .stream()
                .forEach(this::addBean);
        runAll();
    }

    private void addBean(Bean<?> bean) {
        var ctx = bm.createCreationalContext(bean);
        var ref = bm.getReference(bean, bean.getBeanClass(), ctx);
        if (ref instanceof Task task) {
            log.debug("Adding task {}", task.getName());
            addTask((Task) task);
        }else{
            log.error("Bean {} is not a Task", bean);
        }
    }

    private void printTasks(List<Task> tasks) {
        tasks.stream().map(Task::getName).forEach(log::debug);
        return;
    }

    private void addAll(List<Task> tasks) {
        tasks.forEach(this::addTask);
    }

    public void addTask(Task task) {
        if (task.isWrite()) {
            writeQueue.addFirst(task);
        }else {
            readQueue.addLast(task);
        }
        debug("Added", task);
    }

    static final String LOG_FORMAT = "{} [{}] [R {}/W {}]";
    private void debug(String message, Task task) {
        log.debug(LOG_FORMAT,
                message,
                task,
                readQueue.size() ,
                writeQueue.size());
    }

    private void info(String message, Task task) {
        log.info(LOG_FORMAT,
                message,
                task,
                readQueue.size(),
                writeQueue.size());
    }

    private void runAll() {
        log.debug("Processing read task queue");
        runReads();
        log.debug("Processing write task queue [dryRun={}]", config.isDryRun());
        runWrites();
        log.debug("Task queues empty. Done!");
    }

    private void runWrites() {
        while (!writeQueue.isEmpty()) {
            var task = writeQueue.pop();
            config.waitBeforeRun();
            runTask(task);
        }
    }

    private void runReads() {
        while (!readQueue.isEmpty()) {
            var task = readQueue.pop();
            config.waitBeforeRun();
            runTask(task);
        }
    }

    private void runTask(Task task) {
        var isWrite = task.isWrite();
        if (isWrite && config.isDryRun()){
            debug("Skipped", task);
            return;
        }
        tryRun(task);
    }

    private void tryRun(Task task) {
        try {
            task.run();
            debug("Executed {}", task);
        }catch (Throwable ex) {
            int retries = task.getRetries();
            //ex.printStackTrace();
            if (retries > 0) {
                debug("Re-queued", task);
                task.retried();
                addTask(task);
            }else {
                debug("Failed", task);
                //log.error(ex.getMessage(),ex);
            }
        }
    }

    private boolean match(Task p) {
        var match = p.filter(config.getTaskName());
        return match;
    }


}
