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
        delegate.inputs().putAll(self.inputs());
        return tasks().submitTask(delegate);
    }

}
