package cj.ocp;

import cj.shell.CheckShellCommandExistsTask;
import cj.fs.FSUtils;


import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;

import java.nio.file.Path;

import static cj.Input.cj.*;
import static cj.Input.shell.cmd;

import cj.aws.AWSWrite;

@Dependent
@Named("ocp-create-cluster")
public class CreateCluster extends AWSWrite {
    private static final String[] INSTALL_CCOCTL = {"/bin/bash", "-c", "mkdir -p '/tmp/ccoctl' && wget -nv -O '/tmp/ccoctl/ccoctl-linux.tar.gz' 'https://mirror.openshift.com/pub/openshift-v4/x86_64/clients/ocp/latest/ccoctl-linux.tar.gz' && tar zxvf '/tmp/ccoctl/ccoctl-linux.tar.gz' -C '/tmp/ccoctl' && find /tmp/ccoctl/ && sudo mv '/tmp/ccoctl/ccoctl' '/usr/local/bin/' && rm '/tmp/oc/openshift-client-linux.tar.gz'"};
    private static final String[] INSTALL_OPENSHIFT_INSTALL = {"/bin/bash", "-c", "mkdir -p '/tmp/openshift-installer' && wget -nv -O '/tmp/openshift-installer/openshift-install-linux.tar.gz' 'https://mirror.openshift.com/pub/openshift-v4/x86_64/clients/ocp/latest/openshift-install-linux.tar.gz' && tar zxvf '/tmp/openshift-installer/openshift-install-linux.tar.gz' -C '/tmp/openshift-installer' && sudo mv  '/tmp/openshift-installer/openshift-install' '/usr/local/bin/' && rm '/tmp/openshift-installer/openshift-install-linux.tar.gz'"};
    @Inject
    Instance<CheckShellCommandExistsTask> checkCmd;

    enum InstallProfile {
        aws_ipi_sts,
    }

    @Override
    public void apply() {
        debug("ocp-create-cluster");
        var clusterName = inputString(ocp.clusterName).orElse(getExecutionId());
        checkCommands();
        preInstall(clusterName);
        debug("ocp-create-cluster done");
    }

    private void preInstall(String clusterName) {
        debug("preInstall({}, {})", clusterName);
        var clusterDir = getClusterDir(clusterName);
        var credsDir = FSUtils.resolve(clusterDir, "ccoctl-creds");
        var outputDir = FSUtils.resolve(clusterDir, "ccoctl-output");
        var clusterRegion = aws().getRegion().toString();
        createAllCcoctlResources(clusterName, clusterRegion, clusterDir, credsDir, outputDir);
        createInstallConfig(clusterName);
    }

    private void createInstallConfig(String clusterName) {

    }

    private Path getClusterDir(String clusterName) {
        var clusterDir = getTaskDir(clusterName);
        debug("Creating cluster using dir {} ", clusterDir);
        return clusterDir;
    }

    private void createAllCcoctlResources(String clusterName,
                                          String clusterRegion,
                                          Path clusterDir,
                                          Path credsDir,
                                          Path outputDir) {
        var ccoctlExec = exec("ccoctl",
                "aws",
                "create-all",
                "--name="+clusterName,
                "--region="+getRegion().toString(),
                "--credentials-requests-dir="+credsDir.toString(),
                "--output-dir="+outputDir);

        if (ccoctlExec.isPresent()){
            logger().debug("ccoctl returned: {}", ccoctlExec.get());
        }else{
            throw fail("ccoctl failed.");
        }
    }

    private void checkCommands() {
        checkCmd("ccoctl", INSTALL_CCOCTL);
        checkCmd("openshift-install", INSTALL_OPENSHIFT_INSTALL);
    }

    private void checkCmd(String executable, String[] installCmd) {
        var checkTask = withInput(checkCmd, cmd, executable);
        var installTask = shellTask(installCmd);
        retry(checkTask, installTask);
    }

}
