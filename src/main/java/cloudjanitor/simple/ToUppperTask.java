package cloudjanitor.simple;

import cloudjanitor.BaseTask;
import cloudjanitor.Output;
import cloudjanitor.spi.Task;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

import static cloudjanitor.Output.Sample.Message;
import static cloudjanitor.Output.Sample.UpperMessage;

@Dependent
@Named("ToUpper")
public class ToUppperTask extends BaseTask {
    @Inject
    HelloTask hello;

    @Override
    public Task getDependency(){
        return hello;
    }

    @Override
    public void apply() {
        var message = outputString(Message);
        if(message.isPresent()){
            var upper_message = message.get().toUpperCase();
            log().info("Your capitalized message is: {}", upper_message);
            success(UpperMessage, upper_message);
        }else{
            error("message to up not found");
        }
    }


    @Override
    public boolean isWrite() {
        return false;
    }
}
