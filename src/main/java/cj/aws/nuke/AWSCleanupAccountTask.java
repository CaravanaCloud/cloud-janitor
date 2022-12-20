package cj.aws.nuke;

import cj.*;
import cj.aws.AWSInput;
import cj.aws.AWSTask;

import javax.enterprise.context.Dependent;
import javax.inject.Named;

import static cj.TaskMaturity.Level.experimental;

@Dependent
@Named("aws-cleanup")
@TaskMaturity(experimental)
@TaskDescription("Runs aws-nuke for a single account")
@TaskTemplate(value="aws-nuke.qute.yaml", output="aws-nuke.yaml")
@TaskRepeater(TaskRepeat.each_identity)
@SuppressWarnings("unused")
public class AWSCleanupAccountTask extends AWSTask {
    @Override
    public void apply() {
        var accountId = getInputString(AWSInput.accountId);
        debug("aws-nuke started for account {}", accountId);
        runNuke();
        debug("aws-nuke completed.");
    }

    private void runNuke() {
        var dryRun = getInputString(AWSNukeInput.dryRunFlag);
        //TODO: Add these replacements to bypass
        var cmd = new String[]{
                "aws-nuke"
                , dryRun
                ,"--force"
                ,"--config"
                , taskFile("aws-nuke.yaml").toString()
        };
        checkpoint("Executing aws-nuke: {}",
                String.join(" ", cmd));
        shell().exec(cmd);
    }
}
