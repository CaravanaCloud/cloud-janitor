package rooster.impl;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

public class ProcessFlow extends BaseFlow {
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Predicate<String> predicate;
    private String line;

    public ProcessFlow() {
        super();
    }

    public ProcessFlow(Predicate<String> predicate,
        String line) {
        super();
        this.predicate = predicate;
        this.line = line;
    }

    @Override
    public void run() {
        process(predicate, line);
    }

    Optional<String> process(Predicate<String> predicate,
            String line) {
        var args = line.split(" ");
        return process(predicate, args);
    }

    Optional<String> process(Predicate<String> predicate, String... args) {
        String argsLine = String.join(" ", args);
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(args);
        // builder.command("ls");
        builder.directory(new File(System.getProperty("user.home")));
        Process process;
        try {
            process = builder.start();
            var gobbler = new StreamGobbler(process.getInputStream());
            executor.submit(gobbler);
            int exitCode = process.waitFor();
            var result = Optional.of(gobbler.getOutput());
            assertThat(argsLine, exitCode, result, predicate);
            return result;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return Optional.empty();
    }

    void assertThat(String line,
        Integer exitCode,
        Optional<String> result, 
        Predicate<String> consumer) {
        if (result.isPresent()) {
            var output = result.get();
            if (consumer.test(output)) {
                log.info("\uD83D\uDFE2 [PASS "+exitCode+"] "+ line +" : " + trunc(output));
            } else {
                log.info("\uD83D\uDD34 [FAIL "+exitCode+"] "+ line +" : " + trunc(output));
            }
        } else {
            log.info("\uD83D\uDFE2️ [EMPTY "+exitCode+"] "+ line);
        }
    }

    private String trunc(String s) {
        int n = 50;
        return s.substring(0, Math.min(s.length(), n));
    }

}
