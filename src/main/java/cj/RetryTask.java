package cj;

import cj.spi.Task;

import javax.enterprise.context.Dependent;

@Dependent
public class RetryTask extends BaseTask {
    @Override
    public void apply() {
        var task = getInput(CJInput.task, Task.class);
        try {
            info("Trying task {}", task);
            tasks.submit(task);
        } catch (Exception e){
            //TODO: Consider TaskFailedException instead of Exception
            info("Task {} failed {}", task.getName() , e.getMessage());
            var fix = inputAs(CJInput.fixTask, Task.class);
            if (fix.isPresent()){
                var fixTask = fix.get();
                info("Submitting fix task {}", fixTask);
                submit(fixTask);
                info("Retrying task {}", task);
                submit(task);
            }else {
                warn("No fix task provided for {}", task);
                throw fail("Task {} failed", task.getName());
            }

        }

    }
}
