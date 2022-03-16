package tasktree;

import org.slf4j.Logger;
import tasktree.spi.Task;
import tasktree.visitor.PrintTreeVisitor;
import tasktree.visitor.SyncExecutionVisitor;

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
    SyncExecutionVisitor visitor;

    public void runAll(String[] args) {
        config.init(args);
        var taskName = config.getTaskName();
        runByName(taskName);
    }

    private void runByName(String taskName) {
        var tasks = bm.getBeans(taskName)
                .stream()
                .map(this::fromBean)
                .toList();
        for (var task : tasks) {
            visitor.visit(task);
            logTree(task);
        }
    }

    private void logTree(Task task) {
        var visitor = new PrintTreeVisitor();
        visitor.visit(task);
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
