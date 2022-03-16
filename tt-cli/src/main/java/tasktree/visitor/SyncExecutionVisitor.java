package tasktree.visitor;

import org.slf4j.Logger;
import tasktree.Configuration;
import tasktree.spi.Task;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
public class SyncExecutionVisitor {

    @Inject
    Logger log;

    @Inject
    Configuration configuration;

    public void visit(Task task) {
        configuration.runTask(task);
        for(Task child : task.getSubtasks()) {
            visit(child);
        }
    }



}
