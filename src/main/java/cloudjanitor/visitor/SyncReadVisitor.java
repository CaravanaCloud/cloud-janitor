package cloudjanitor.visitor;

import cloudjanitor.Configuration;
import cloudjanitor.spi.Task;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
public class SyncReadVisitor extends TaskVisitor {
    @Inject
    Configuration configuration;

    public void read(Task task) {
        collect(configuration.runTask(task));
    }
}
