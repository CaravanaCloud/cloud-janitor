package cj.aws.nuke;

import cj.*;
import cj.aws.AWSInput;
import cj.aws.AWSTask;

import javax.enterprise.context.Dependent;
import javax.inject.Named;
import java.nio.file.Path;

import static cj.Capabilities.*;

@Dependent
@Named("aws-nuke-account")
@TaskTemplate(value="aws-nuke.qute.yaml", output="aws-nuke.yaml")
@SuppressWarnings("unused")
public class AWSNukeAccountTask extends AWSTask {
    @Override
    public void apply() {
        var accountId = getInputString(AWSInput.accountId);
        debug("aws-nuke started for account {}", accountId);
        runNuke();
        debug("aws-nuke completed.");
    }

    private void runNuke() {
        var dryRun = hasCapabilities(CLOUD_DELETE_RESOURCES) ?
                "--no-dry-run" :
                "";
        var cmd = new String[]{
                "aws-nuke"
                , dryRun
                ,"--force"
                ,"--config"
                , taskFile("aws-nuke.yaml").toString()
        };
        checkpoint("Executing aws-nuke: {}",
                String.join(" ", cmd));
        tasks().shell(cmd);
    }
}
