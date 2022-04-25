package cloudjanitor.visitor;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import cloudjanitor.Result;
import cloudjanitor.spi.Task;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

@Dependent
public class CSVVisitor extends TaskVisitor {
    @Inject
    Logger log;

    @ConfigProperty(name = "tt.csv.output", defaultValue = "cloudjanitor.csv")
    String fileName;

    Writer out;


    public void read(Task task) {
        collect(task.getResult());
    }

    public void write(Task task) {
        collect(task.getResult());
    }

    @Override
    public void after(Task task) {
        out = switch (fileName) {
            case "" -> {
                log.info("Writing to stdout");
                yield new PrintWriter(System.out);
            }
            default -> {
                try {
                    log.info("Writing to {}", fileName);
                    yield new FileWriter(fileName);
                } catch (IOException e) {
                    e.printStackTrace();
                    log.info("Writing to stderr");
                    yield new PrintWriter(System.err);
                }
            }
        };
        var results = getResults();
        log.info("Writing {} results to CSV", results.size());
        var format = CSVFormat.EXCEL.builder()
                .setEscape('\\')
                .setDelimiter(',')
                .setQuote('"')
                .setQuoteMode(QuoteMode.ALL)
                .setHeader(Result.getHeader())
                .build();
        try (var printer = new CSVPrinter(out, format)) {
            results.stream().forEach(r -> emmit(printer, r));
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("CSV file written to {}", fileName);
    }

    private void emmit(CSVPrinter printer, Result result) {
        if (result == null){
            log.warn("Can't emmit null result");
            return;
        }
        try {
            printer.printRecord(result.toRecord());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
