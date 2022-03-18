package tasktree;

import org.slf4j.Logger;
import tasktree.spi.Task;
import tasktree.visitor.PrintTreeVisitor;
import tasktree.visitor.SyncReadVisitor;
import tasktree.visitor.SyncWriteVisitor;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

@Dependent
public class TaskManager {
    @Inject
    BeanManager bm;

    @Inject
    Configuration config;

    @Inject
    Logger log;

    @Inject
    SyncReadVisitor reads;

    @Inject
    SyncWriteVisitor writes;


    public void runAll(String[] args) {
        config.init(args);
        var taskName = config.getTaskName();
        runByName(taskName);
    }

    private void runByName(String taskName) {
        bm.getBeans(taskName)
                .stream()
                .map(this::fromBean)
                .forEach(this::runTask);
    }

    private void runTask(Task task) {
        task.accept(reads);
        logTree(task);
        if(config.isDryRun()){
           log.info("Dry run, skipping writes");
        }else {
            task.accept(writes);
            logTree(task);
        }

    }

    private void logTree(Task task) {
        var print = new PrintTreeVisitor();
        task.accept(print);
    }

    private Task fromBean(Bean<?> bean) {
        var ctx = bm.createCreationalContext(bean);
        var ref = bm.getReference(bean, bean.getBeanClass(), ctx);
        if (ref instanceof Task task) {
            return (Task) task;
        }else{
            log.error("Bean {} is not a Task", bean);
            return null;
        }
    }

}
