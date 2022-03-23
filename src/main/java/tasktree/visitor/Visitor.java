package tasktree.visitor;

import tasktree.Result;
import tasktree.spi.Task;

import java.util.List;

public interface Visitor {
    default void read(Task task){}
    default void write(Task task){}
    default void before(Task task){}
    default void after(Task task){}
    default List<Result> getResults(){return List.of();}
}
