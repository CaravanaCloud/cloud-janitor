package cloudjanitor.reporting;

import cloudjanitor.Tasks;
import cloudjanitor.spi.Task;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

@ApplicationScoped
public class Reporting {
    @ConfigProperty(name = "cj.reporting.output.file", defaultValue = ".cloud-janitor.html")
    String outputFile;
    @Location("cloud-janitor.html")
    Template report;

    @Inject
    Logger log;

    public void report(Tasks tasks) {
        String result = report
                .data("tasks", tasks)
                .render();
        write(result);
    }


    private void write(String result) {
        try (var out = new PrintWriter(outputFile)) {
            out.println(result);
        } catch (FileNotFoundException e) {
            log.error("Failed to write report", e);
            throw new RuntimeException(e);
        }
        log.info("Report write successful.");
    }
}
