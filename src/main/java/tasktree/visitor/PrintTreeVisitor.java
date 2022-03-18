package tasktree.visitor;

import tasktree.spi.Task;

import javax.enterprise.context.Dependent;
import java.io.IOException;

@Dependent
public class PrintTreeVisitor {
    static final String PADDING = "-";
    StringBuilder buf = new StringBuilder();

    public void visit(Task task) {
        visit(task, "");
        var out = System.out;
        out.println(buf.toString());
    }

    private void visit(Task task, String padding) {
        if (task == null) return;
        buf.append(padding);
        buf.append(task);
        buf.append("\n");
        var subs = task.getSubtasks();
        for (var sub : subs) {
            visit(sub, padding + PADDING);
        }
    }
}
