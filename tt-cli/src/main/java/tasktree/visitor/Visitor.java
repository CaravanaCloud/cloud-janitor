package tasktree.visitor;

import tasktree.spi.Task;

public interface Visitor {
    void visit(Task task);
}
