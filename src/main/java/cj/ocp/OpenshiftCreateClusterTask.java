package cj.ocp;

import cj.BaseTask;
import cj.Capabilities;
import cj.Input;
import cj.OS;
import cj.fs.FSUtils;

import javax.enterprise.context.Dependent;
import javax.inject.Named;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Dependent
@Named("openshift-create-cluster")
@SuppressWarnings("unused")
public class OpenshiftCreateClusterTask extends BaseTask {

    @Override
    public List<Input> getExpectedInputs() {
        return List.of(OCPInput.clusterName,
                OCPInput.baseDomain,
                OCPInput.sshKey,
                OCPInput.pullSecret,
                OCPInput.awsRegion,
                OCPInput.clusterProfile);
    }

    @Override
    public void apply() {
        debug("ocp-create-cluster");
        var clusterName = getInputString(OCPInput.clusterName);
        var clusterProfile = getInput(OCPInput.clusterProfile, ClusterProfile.class);
        debug("creating cluster [{}] with profile [{}]", clusterName, clusterProfile);
        var clusterDir = getClusterDir(clusterName);
        if (! FSUtils.isEmptyDir(clusterDir))
            throw fail("Cluster directory already exists %s ", clusterDir);
        else
            debug("Using cluster dir {}", clusterDir);
        var credsDir = FSUtils.resolve(clusterDir, "ccoctl-creds");
        var outputDir = FSUtils.resolve(clusterDir, "ccoctl-output");
        checkCommands(clusterProfile);
        var data = getInputsMap();
        preCreate(clusterName, clusterDir, credsDir, outputDir, clusterProfile, data);
        createCluster(clusterName, clusterDir);
        debug("ocp-create-cluster done");
    }
    private void createCluster(String clusterName, Path clusterDir) {
        var tip = "tail -f "+ clusterDir.resolve(".openshift_install.log").toAbsolutePath();
        debug(tip);
        expectCapability(Capabilities.CLOUD_CREATE_INSTANCES);
        var output = exec(90L, "openshift-install",
                "create",
                "cluster",
                "--dir=" + clusterDir,
                "--log-level=debug");
        if (output.isPresent()){
            logger().debug("openshift-install output: {}", output.get());
        }else{
            throw fail("openshift-install failed.");
        }
    }

    private void preCreate(String clusterName, Path clusterDir, Path credsDir, Path outputDir, ClusterProfile profile, Map<String, String> configData) {
        debug("Preparing to create cluster {} with profile {}", clusterName, profile);
        if (profile.ccoctl){
            createAllCcoctlResources(clusterName, credsDir, outputDir);
        }
        createInstallConfigFromTemplate(clusterDir, clusterName, profile, configData);
    }

    //TODO: Avoid prompts
    @SuppressWarnings("unused")
    private void createDefaultInstallConfig(Path clusterDir, String clusterName) {
        var output = exec( "openshift-install",
                "create",
                "install-config",
                "--dir=" + clusterDir,
                "--log-level=debug");
        if (output.isPresent()){
            logger().debug("openshift create install-config output: {}", output.get());
        }else{
            throw fail("openshift create install-config failed.");
        }
    }

    private void createInstallConfigFromTemplate(Path clusterDir, String clusterName, ClusterProfile profile, Map<String, String> configData) {
        var location = "ocp/%s/install-config.yaml".formatted(profile);
        var installConfigTemplate = getTemplate(location);
        String installConfig = installConfigTemplate
                .data(configData)
                .render();
        Path installConfigPath = clusterDir.resolve("install-config.yaml");
        FSUtils.writeFile(installConfigPath, installConfig);
        Path backupConfigPath = clusterDir.resolve("install-config.bak.yaml");
        FSUtils.writeFile(backupConfigPath, installConfig);
        debug("Wrote [{}] install-config.yaml [{}] to {}", profile, installConfig.length() ,installConfigPath);
    }

    private Path getClusterDir(String clusterName) {
        var clusterDir = getTaskDir(clusterName);
        debug("Creating cluster using dir {} ", clusterDir);
        return clusterDir;
    }

    private void createAllCcoctlResources(String clusterName,
                                          Path credsDir,
                                          Path outputDir) {
        var ccoctlExec = exec("ccoctl",
                "aws",
                "create-all",
                "--name="+clusterName,
                "--region="+inputString(OCPInput.awsRegion),
                "--credentials-requests-dir="+credsDir.toString(),
                "--output-dir="+outputDir);

        if (ccoctlExec.isPresent()){
            logger().debug("ccoctl output: {}", ccoctlExec.get());
        }else{
            throw fail("ccoctl failed.");
        }
    }

    protected void checkCommands(ClusterProfile profile) {
        if(profile.ccoctl) {
            checkCmd("ccoctl", Map.of(OS.linux, OCPCommands.INSTALL_CCOCTL));
        }
        checkCmd("openshift-install", Map.of(OS.linux, OCPCommands.INSTALL_OPENSHIFT_INSTALL));
    }
}
