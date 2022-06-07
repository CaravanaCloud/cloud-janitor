package cloudjanitor.simple;

import cloudjanitor.BaseTask;
import cloudjanitor.Output;
import cloudjanitor.spi.Task;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Dependent
@Named("ToUpper")
public class ToUppperTask extends BaseTask {
    @Inject
    HelloTask hello;

    @Override
    public List<Task> getDependencies() {
        return List.of(hello);
    }

    @Override
    public void runSafe() {
        var message = outputString(Output.Sample.Message);
        if(message.isPresent()){
            var upper_message = message.get().toUpperCase();
            log().info("Your capitalized message is: {}", upper_message);
            success(Output.Sample.UpperMessage, upper_message);
        }else{
            failure("message not found");
        }
    }


    @Override
    public boolean isWrite() {
        return false;
    }
}
