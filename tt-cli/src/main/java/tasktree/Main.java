package tasktree;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.kie.api.task.model.Task;

@QuarkusMain    
public class Main {
    public static void main(String... args) {
        Quarkus.run(TaskTree.class, args);
    }
}

