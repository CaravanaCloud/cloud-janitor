package tasktree;

import org.slf4j.Logger;
import tasktree.spi.Task;
import tasktree.visitor.*;

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

    @Inject
    CSVVisitor csv;


    public void runAll(String[] args) {
        config.init(args);
        var taskName = config.getTaskName();
        runByName(taskName);
    }

    private void runByName(String taskName) {
        bm.getBeans(taskName)
                .stream()
                .map(this::fromBean)
                .forEach(this::tryTask);
    }

    private void tryTask(Task task) {
        runTask(task);
    }

    private void runTask(Task task) {
        accept(task, reads);
        accept(task, new PrintTreeVisitor());
        accept(task, writes);
        accept(task, new PrintTreeVisitor());
        accept(task, csv);
        accept(task, new PrintTableVisitor());
    }

    private void accept(Task task, Visitor visitor) {
        visitor.before(task);
        task.accept(visitor);
        visitor.after(task);
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
