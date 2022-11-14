package cj;

import cj.spi.Task;

public class CompositeTask extends BaseTask {
    @Override
    public void apply() {
        var tasks = inputList(Input.cj.tasks, Task.class);
        info("Executing {} tasks", tasks.size());
        tasks.forEach(this::submit);
    }
}
