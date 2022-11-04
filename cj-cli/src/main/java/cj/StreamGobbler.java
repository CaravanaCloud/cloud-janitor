package cj;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class StreamGobbler implements Runnable {
    private final InputStream inputStream;
    private final Consumer<String> consumer;

    public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
        this.inputStream = inputStream;
        this.consumer = consumer;
    }

    @Override
    public void run() {
        try {
            var text = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            consumer.accept(text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}