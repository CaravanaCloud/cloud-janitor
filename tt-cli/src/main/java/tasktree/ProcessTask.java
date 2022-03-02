package tasktree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tasktree.spi.BaseResult;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

public class ProcessTask extends BaseTask {
    static final Logger log = LoggerFactory.getLogger(ProcessTask.class);

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Predicate<String> predicate;
    private String line;

    public ProcessTask() {
        super();
    }

    public ProcessTask(Predicate<String> predicate,
                       String line) {
        super();
        this.predicate = predicate;
        this.line = line;
    }

    public void run() {
        var result = process(predicate, line);
        log.info(result.toString());
    }

    BaseResult process(Predicate<String> predicate,
                       String line) {
        var args = line.split(" ");
        return process(predicate, args);
    }

    BaseResult process(Predicate<String> predicate, String... args) {
        String argsLine = String.join(" ", args);
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(args);
        builder.directory(new File(System.getProperty("user.home")));
        Process process;
        try {
            process = builder.start();
            var gobbler = new StreamGobbler(process.getInputStream());
            executor.submit(gobbler);
            var exitCode = process.waitFor();
            var output = gobbler.getOutput();
            var pass = assertThat(argsLine, exitCode, output, predicate);
            var sample = BaseResult.exited(argsLine, exitCode, output, pass);
            return sample;
        } catch (IOException | InterruptedException e) {
            return BaseResult.throwing(argsLine, e);
        }
    }

    Boolean assertThat(String line,
                       Integer exitCode,
                       String output,
                       Predicate<String> consumer) {
        if (output != null) {
            return consumer.test(output);
        }
        return null;
    }



}
