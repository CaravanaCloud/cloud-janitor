package cj.aws.nuke;

import cj.OS;
import cj.StartupObserver;
import cj.Tasks;

import javax.inject.Inject;
import java.util.Map;

@SuppressWarnings("unused")
public class AWSNukeStartup extends StartupObserver {
    static final String[] INSTALL_LINUX = {"/bin/bash", "-c", "mkdir -p '/tmp/aws-nuke' && wget -nv -O '/tmp/aws-nuke/aws-nuke-linux.tar.gz' 'https://github.com/rebuy-de/aws-nuke/releases/download/v2.20.0/aws-nuke-v2.20.0-linux-amd64.tar.gz' && tar zxvf '/tmp/aws-nuke/aws-nuke-linux.tar.gz' -C '/tmp/aws-nuke' && sudo mv '/tmp/aws-nuke/aws-nuke-v2.20.0-linux-amd64' '/usr/local/bin/aws-nuke' && rm '/tmp/aws-nuke/aws-nuke-linux.tar.gz'"};

    @Inject
    Tasks tasks;
    @Override
    public void onStart() {
        log().trace("AWS Nuke Startup");
        tasks.mapInstall("aws-nuke", Map.of(OS.linux, INSTALL_LINUX));
    }
}
