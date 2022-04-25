package cloudjanitor.visitor;

import cloudjanitor.Configuration;
import cloudjanitor.spi.Task;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
public class SyncWriteVisitor extends TaskVisitor {
    @Inject
    Configuration configuration;

    public void write(Task task) {
        collect(configuration.runTask(task));
    }
}
