package cj;

import cj.spi.Task;

import javax.enterprise.inject.spi.CDI;

public interface TaskManagement {
    default Tasks tasks() {
        @SuppressWarnings("UnnecessaryLocalVariable")
        var tasks = CDI.current().select(Tasks.class).get();
        return tasks;
    }


    default Task submit(Task self, Task delegate) {
        delegate.getInputs().putAll(self.getInputs());
        return tasks().submit(delegate);
    }

}
