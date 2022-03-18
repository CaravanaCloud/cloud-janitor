package tasktree.visitor;

import tasktree.spi.Task;

import javax.enterprise.context.Dependent;

@Dependent
public class PrintTreeVisitor implements Visitor{
    static final String PADDING = "-";
    StringBuilder buf = new StringBuilder();
    boolean root = true;

    public void read(Task task) {
        if (root) {
            root = false;
            appendString(task, "");
        }
        var out = System.out;
        out.println(buf.toString());
    }

    private void appendString(Task task, String padding) {
        if (task == null) return;
        buf.append(padding);
        buf.append(task);
        buf.append("\n");
        var subs = task.getSubtasks();
        for (var sub : subs) {
            appendString(sub, padding + PADDING);
        }
    }
}
