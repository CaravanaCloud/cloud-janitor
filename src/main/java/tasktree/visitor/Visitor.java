package tasktree.visitor;

import tasktree.spi.Task;

public interface Visitor {
    default void read(Task task){}
    default void write(Task task){}
}
