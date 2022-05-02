package cloudjanitor.simple;

import cloudjanitor.BaseTask;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
public class ToUppperTask extends BaseTask {
    @Inject
    HelloTask hello;

    @PostConstruct
    public void resolveDependencies(){
        dependsOn(hello);
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
