package cloudjanitor.simple;

import cloudjanitor.BaseTask;
import cloudjanitor.spi.Task;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;

@Dependent
public class ToUppperTask extends BaseTask {
    @Inject
    HelloTask hello;

    @Override
    public List<Task> getDependencies() {
        return List.of(hello);
    }

    @Override
    public void runSafe() {
        var message = findString("message");
        if(message.isPresent()){
            success("upper_message", message.get().toUpperCase());
        }else{
            failure("message not found");
        }
    }


    @Override
    public boolean isWrite() {
        return false;
    }
}
