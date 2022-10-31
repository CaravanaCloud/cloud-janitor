package cj.hello;

import cj.BaseTask;
import cj.spi.Task;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

import static cj.Output.Sample.Message;
import static cj.Output.Sample.UpperMessage;

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
            info("Your capitalized message is: {}", upper_message);
            success(UpperMessage, upper_message);
        }else{
            fail("message to up not found");
        }
    }


    @Override
    public boolean isWrite() {
        return false;
    }
}
