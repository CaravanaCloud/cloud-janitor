package cloudjanitor.visitor;

import cloudjanitor.Result;
import cloudjanitor.spi.Task;

import java.util.Comparator;

public class PrintTableVisitor extends TaskVisitor {

    public void read(Task task) {
        collect(task.getResult());
    }

    public void write(Task task) {
        collect(task.getResult());
    }

    @Override
    public void after(Task task) {
        var results = getResults();
        results.sort(Comparator.comparing(Result::getStartTime));
        var resultsStr = results.stream()
                .map(r -> format(r))
                .toList();
        var str = "\n" + String.join("\n", resultsStr);
        log.info(str);
    }

    // static final String FORMAT = "%32s%10d%16s";
    static final String FORMAT = "|%20.20s|%22.22s|%10.10s|%60.60s|";
    private String format(Result r) {
        return  FORMAT.formatted(
                r.startTimeISO(),
                r.getTaskName(),
                r.getType().toString(),
                r.getDescription());
    }

}
