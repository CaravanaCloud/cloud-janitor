package cloudjanitor.reporting;

import cloudjanitor.spi.Task;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

@ApplicationScoped
public class Reporting {
    @Location("cloud-janitor.html")
    Template report;

    @Inject
    Logger log;

    public void report(List<Task> matches) {
        String result = report
                .data("now", java.time.LocalDateTime.now())
                .render();
        write(result);
    }

    private void write(String result) {
        try (var out = new PrintWriter("cloud-janitor.html")) {
            out.println(result);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        log.info("Report write successful.");
    }
}
