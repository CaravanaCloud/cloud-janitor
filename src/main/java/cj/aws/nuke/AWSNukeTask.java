package cj.aws.nuke;

import javax.enterprise.context.Dependent;
import javax.inject.Named;

import cj.*;
import cj.TaskMaturity.Level;
import cj.aws.AWSTask;
import cj.fs.FSUtils;
import cj.ocp.OCPCommands;

import java.util.Map;

@Dependent
@Named("aws-nuke")
@TaskDescription("Runs aws-nuke")
@TaskMaturity(Level.experimental)
@ExpectedInputs({})
public class AWSNukeTask extends AWSTask {
    static final String CONTEXT = "aws-nuke";

    static final String[] INSTALL_LINUX = {"/bin/bash", "-c", "mkdir -p '/tmp/aws-nuke' && wget -nv -O '/tmp/aws-nuke/aws-nuke-linux.tar.gz' 'https://github.com/rebuy-de/aws-nuke/releases/download/v2.20.0/aws-nuke-v2.20.0-linux-amd64.tar.gz' && tar zxvf '/tmp/aws-nuke/aws-nuke-linux.tar.gz' -C '/tmp/aws-nuke' && sudo mv '/tmp/aws-nuke/aws-nuke-v2.20.0-linux-amd64' '/usr/local/bin/aws-nuke' && rm '/tmp/aws-nuke/aws-nuke-linux.tar.gz'"};

    @Override
    public void apply() {
        debug("aws-nuke");
        var taskDir = taskDir();
        var profile = "minimal";
        var cfgTemplate = "aws-nuke/%s/aws-nuke.qute.yaml".formatted(profile);
        var cfgFile = taskDir.resolve("aws-nuke.yaml");
        debug("Running aws-nuke from {}", taskDir);
        var awsIdentity = getIdentity();
        var accountId = awsIdentity.getAccountId();
        var inputs = Map.of(
                "accountId", accountId
        );
        var cfgBody = render(cfgTemplate, inputs);
        FSUtils.writeFile(cfgFile, cfgBody);
        debug("Written aws-nuke config file: cat {}", cfgFile);
        checkCmd();
        var dryRun = hasCapabilities(Capabilities.CLOUD_DELETE_RESOURCES) ?
                "--dry-run" :
                "";
        var cmd = new String[]{
                "aws-nuke"
                , dryRun
                ,"--force"
                ,"--config"
                , cfgFile.toString()
        };
        var line = String.join(" ", cmd);
        debug("exec: {}", line);
        tasks().exec(cmd);
    }

    private void checkCmd() {
        tasks().checkCmd("aws-nuke", Map.of(OS.linux, INSTALL_LINUX));
    }

}
