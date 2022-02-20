package tasktree;

import tasktree.spi.Sample;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

public class ProcessProbe extends BaseProbe {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Predicate<String> predicate;
    private String line;

    public ProcessProbe() {
        super();
    }

    public ProcessProbe(Predicate<String> predicate,
                        String line) {
        super();
        this.predicate = predicate;
        this.line = line;
    }

    @Override
    public Sample call() {
        return process(predicate, line);
    }

    Sample process(Predicate<String> predicate,
            String line) {
        var args = line.split(" ");
        return process(predicate, args);
    }

    Sample process(Predicate<String> predicate, String... args) {
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
            var sample = Sample.exited(argsLine, exitCode, output, pass);
            return sample;
        } catch (IOException | InterruptedException e) {
            return Sample.throwing(argsLine, e);
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
