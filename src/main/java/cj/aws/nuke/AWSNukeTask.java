package cj.aws.nuke;

import java.nio.file.Path;

import javax.enterprise.context.Dependent;
import javax.inject.Named;

import cj.BaseTask;
import cj.ExpectedInputs;
import cj.TaskDescription;
import cj.TaskMaturity;
import cj.TaskMaturity.Level;
import cj.fs.FSUtils;

@Dependent
@Named("aws-nuke")
@TaskDescription("Runs aws-nuke")
@TaskMaturity(Level.experimental)
@ExpectedInputs({})
public class AWSNukeTask extends BaseTask {
    @Override
    public void apply() {
        debug("aws-nuke");
        var configPath = getTaskDir();
        debug("Running aws-nuke from {}", configPath);
        /*
        var clusterDir = getTaskDir()
        var templateName =  "%s_%s_%s".formatted(
            cloudProvider.name().toLowerCase(),
            infrastrucutreProvider.name().toLowerCase(),
            profile.name().toLowerCase());
    var location = "ocp/%s/install-config.yaml".formatted(templateName);
    String output = render(location, data);

        generateConfigFile();
         */
    }

    private Path getTaskDir() {
        var appDir = FSUtils.getTasksDir();
        var taskName = getPathName();
        var taskDir = FSUtils.resolve(appDir, taskName);
        return taskDir;
    }

    private String getPathName() {
        return getName();
    }
}
