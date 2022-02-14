package rooster.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;

public class StreamGobbler implements Runnable {
    private static final String LINE_SEP = System.getProperty("line.separator");
    private InputStream inputStream;
    private StringBuilder output = new StringBuilder();

    public StreamGobbler(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public void run() {
        var reader =  new BufferedReader(new InputStreamReader(inputStream));
        var lines = reader.lines();
        lines.forEach(line -> output.append(line + LINE_SEP));
    }

    public String getOutput() {
        return output.toString();
    }
}