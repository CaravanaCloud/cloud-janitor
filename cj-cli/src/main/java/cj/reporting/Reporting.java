package cj.reporting;

import cj.Configuration;
import cj.Tasks;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

@ApplicationScoped
public class Reporting {
    @Location("cloud-janitor.html")
    Template report;

    @Inject
    Logger log;

    @Inject
    Configuration config;


    public void report(Tasks tasks) {
        copyReportTemplate();
//        replaceTemplateVariables();
//        sucess();
    }

    private void copyReportTemplate() {
        var templateName = config.templateName();
        var filesToCopy = lookupFilesToCopy(templateName);
        var reportsPath = config.getReportsPath();
        log.debug("Copying {} files from template", filesToCopy.size());
    }
    public List<String> lookupFilesToCopy(String templateName){
        var basePath = "cj-templates";
        var templatePath = templateName;
        var descriptorPath = "/%s/%s/%s".formatted(basePath, templatePath, "cj-template.txt");
        try (var in = getClass().getClassLoader().getResourceAsStream(descriptorPath)) {
            if (in == null) {
                return List.of();
            }
            var reader = new BufferedReader(
                    new InputStreamReader(in, StandardCharsets.UTF_8));
            var lines = reader.lines().toList();
            return lines;
        } catch (IOException e) {
            log.error("Error reading file " + descriptorPath, e);
        }
        return List.of();
    }


    public void oldReport(Tasks tasks) {
        String result = report
                .data("tasks", tasks)
                .render();
        write(result);
    }


    private void write(String result) {
        var appPath = config.getApplicationPath();
        var reportFile = appPath.resolve(config.report().outputFile()).toFile();
        try (var out = new PrintWriter(reportFile)) {
            out.println(result);
        } catch (FileNotFoundException e) {
            log.error("Failed to write report", e);
            throw new RuntimeException(e);
        }
        log.info("Report write successful. {}", reportFile.getAbsolutePath());
    }
}
