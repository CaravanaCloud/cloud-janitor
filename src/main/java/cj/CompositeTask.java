package cj;

import cj.spi.Task;

public class CompositeTask extends BaseTask {
    @Override
    public void apply() {
        var tasks = inputList(CJInput.taskNames, Task.class);
        info("Executing {} tasks", tasks.size());
        tasks.forEach(this::submit);
    }
}
