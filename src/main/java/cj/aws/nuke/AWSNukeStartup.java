package cj.aws.nuke;

import cj.Input;
import cj.OS;
import cj.StartupObserver;

import java.util.Map;

import static cj.Capabilities.CLOUD_DELETE_RESOURCES;
import static cj.aws.nuke.AWSNukeInput.*;

@SuppressWarnings("unused")
public class AWSNukeStartup extends StartupObserver {
    static final String[] INSTALL_LINUX = {"/bin/bash", "-c", "mkdir -p '/tmp/aws-nuke' && wget -nv -O '/tmp/aws-nuke/aws-nuke-linux.tar.gz' 'https://github.com/rebuy-de/aws-nuke/releases/download/v2.20.0/aws-nuke-v2.20.0-linux-amd64.tar.gz' && tar zxvf '/tmp/aws-nuke/aws-nuke-linux.tar.gz' -C '/tmp/aws-nuke' && sudo mv '/tmp/aws-nuke/aws-nuke-v2.20.0-linux-amd64' '/usr/local/bin/aws-nuke' && rm '/tmp/aws-nuke/aws-nuke-linux.tar.gz'"};
    static final String TASK_NAME = "aws-nuke";
    @Override
    public void onStart() {
        log().trace("AWS Nuke Startup");
        tasks().mapInstall("aws-nuke", Map.of(OS.linux, INSTALL_LINUX));
        describeInput(forceFlag,
                "aws-nuke force setting",
                "not available",
                null,
                () -> "--force",
                "Alwayrs force",
                null);
        describeInput(dryRunFlag,
                "aws-nuke dry-run setting",
                "not available",
                null,
                () -> tasks().hasCapabilities(CLOUD_DELETE_RESOURCES) ? "" : "--dry-run",
                "Dry run unless CLOUD_DELETE_RESOURCES capability is set",
                null,
                true);
        describeInput(configFlag,
                "aws-nuke config file setting",
                "not available",
                null,
                () -> "--config=" + tasks().taskFile(TASK_NAME, "ccsandbox.yaml").toString(),
                "Dry run unless CLOUD_DELETE_RESOURCES capability is set",
                null,
                true);

        //TODO: Create metadata maps for
        //@TaskTemplate(value="aws-nuke.qute.yaml", output="ccsandbox.yaml")
        //@TaskMaturity(experimental)
        //@TaskDescription("Runs aws-nuke for a single account")
        //forEachIdentity
    }
    protected void enrichBypass(String taskName, Input... inputs) {
        tasks().enrichBypass(taskName, inputs);
    }
}
