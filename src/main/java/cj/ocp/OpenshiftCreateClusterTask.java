package cj.ocp;

import cj.*;
import cj.cloud.CloudInputs;
import cj.cloud.CloudProvider;
import cj.fs.TaskFiles;

import javax.enterprise.context.Dependent;
import javax.inject.Named;
import java.nio.file.Path;
import java.util.Map;

import static cj.TaskMaturity.Level.experimental;

@Dependent
@Named("openshift-create-cluster")
@TaskDescription("Creates an OpenShift cluster")
@TaskMaturity(experimental)
@ExpectedInputs({"clusterName",
        "baseDomain",
        "sshKey",
        "pullSecret",
        "awsRegion",
        "clusterProfile",
        "instanceType",
        "infrastructureProvider",
})
@SuppressWarnings("unused")
public class OpenshiftCreateClusterTask extends BaseTask {

    @Override
    public void apply() {
        debug("ocp-create-cluster");
        var clusterName = getInputString(OCPInput.clusterName);
        var clusterProfile = getInput(OCPInput.clusterProfile, ClusterProfile.class);
        debug("creating cluster [{}] with profile [{}]", clusterName, clusterProfile);
        var clusterDir = getClusterDir(clusterName);
        if (!TaskFiles.isEmptyDir(clusterDir))
            throw fail("Cluster directory already exists %s ", clusterDir);
        else
            debug("Using cluster dir {}", clusterDir);
        var credsDir = TaskFiles.resolveDir(clusterDir, "ccoctl-creds");
        var outputDir = TaskFiles.resolveDir(clusterDir, "ccoctl-output");
        checkCommands(clusterProfile);
        preCreate(clusterName, clusterDir, credsDir, outputDir, clusterProfile);
        createCluster(clusterName, clusterDir);
        var kubeconfigdir = clusterDir.resolve("auth");
        export("KUBECONFIG", kubeconfigdir);
        var home = TaskFiles.homePath();
        var defaultkubedir = TaskFiles.resolveDir(home,".kube");
        TaskFiles.copyDirWithBackup(kubeconfigdir, defaultkubedir);
        var newkubeconfig = defaultkubedir.resolve("kubeconfig");
        var newconfig = defaultkubedir.resolve("config");
        TaskFiles.copyFileWithBackup(newkubeconfig, newconfig);
        debug("ocp-create-cluster done");
    }

    private void link(Path kubeconfig, Path defaultKubeconfig) {
        var cmd = new String[]{
            "ln",
            "-sf",
            kubeconfig.toString(),
            defaultKubeconfig.toString()
        };
        shell().exec(cmd);
    }

    private void export(String varName, Path varValue) {
        TaskFiles.writeEnv(varName, varValue);
    }

    protected void checkCommands(ClusterProfile profile) {
        if (profile.ccoctl) {
            shell().checkCmd("ccoctl", Map.of(OS.linux, OCPCommands.INSTALL_CCOCTL));
        }
        shell().checkCmd("openshift-install", Map.of(OS.linux,
                OCPCommands.INSTALL_OPENSHIFT_INSTALL));
    }


    private void createCluster(String clusterName, Path clusterDir) {
        var tip = "tail -f " + clusterDir.resolve(".openshift_install.log").toAbsolutePath();
        debug(tip);
        checkCapability(Capabilities.CLOUD_CREATE_INSTANCES);
        var cmdArgs = new String[]{
                "openshift-install",
                "create",
                "cluster",
                "--dir=" + clusterDir,
                "--log-level=debug"
        };
        debug("Running command: {}", String.join(" ", cmdArgs));
        checkpoint("Creating openshift cluster using openshift-install");
        var exec = shell().exec(90L, cmdArgs);
        if (exec.isSuccess()) {
            log().debug("openshift-install output size: {}", exec.stdout().length());
        } else {
            throw fail("openshift-install failed.");
        }
    }

    private void preCreate(String clusterName, Path clusterDir, Path credsDir, Path outputDir, ClusterProfile profile) {
        debug("Preparing to create cluster {} with profile {}", clusterName, profile);
        if (profile.ccoctl) {
            createAllCcoctlResources(clusterName, credsDir, outputDir);
        }
        createInstallConfigFromTemplate(clusterDir, clusterName, profile);
    }

    private void createInstallConfigFromTemplate(Path clusterDir, String clusterName, ClusterProfile profile) {
        var cloudProvider = getInput(CloudInputs.cloudProvider, CloudProvider.class);
        var infrastructureProvider = getInput(OCPInput.infrastructureProvider, OCPInfrastructureProvider.class);
        var profileName = profile.toString();
        var output = render(profileName, "install-config.quote.yaml", "install-config.yaml");
        var installConfigPath = clusterDir.resolve("install-config.yaml");
        var backupConfigPath = clusterDir.resolve("install-config.bak.yaml");
        TaskFiles.copyFileWithBackup(installConfigPath, backupConfigPath);
        debug("Wrote [{}] install-config.yaml to {}", profile, installConfigPath);
    }


    private Path getClusterDir(String clusterName) {
        var clusterDir = TaskFiles.dataDir("openshift-cluster", clusterName);
        debug("Creating cluster using dir {} ", clusterDir);
        return clusterDir;
    }

    private void createAllCcoctlResources(String clusterName,
            Path credsDir,
            Path outputDir) {
        var ccoctlExec = shell().exec("ccoctl",
                "aws",
                "create-all",
                "--name=" + clusterName,
                "--region=" + inputString(OCPInput.awsRegion),
                "--credentials-requests-dir=" + credsDir.toString(),
                "--output-dir=" + outputDir);
        if (ccoctlExec.isSuccess()) {
            log().debug("ccoctl output: {}", ccoctlExec.stdout().length());
        } else {
            throw fail("ccoctl failed.");
        }
    }

}
