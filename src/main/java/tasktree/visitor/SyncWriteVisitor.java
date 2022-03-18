package tasktree.visitor;

import org.slf4j.Logger;
import tasktree.Configuration;
import tasktree.spi.Task;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
public class SyncWriteVisitor implements Visitor {

    @Inject
    Logger log;

    @Inject
    Configuration configuration;

    public void write(Task task) {
            configuration.runTask(task);
    }
}