package cj.ocp;

import cj.Capabilities;
import cj.ConfigurationNotFoundException;
import cj.Input;
import cj.Inputs;
import cj.aws.AWSWrite;
import cj.fs.FSUtils;
import cj.shell.CheckShellCommandExistsTask;
import cj.shell.ShellInput;
import io.quarkus.qute.Engine;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Dependent
@Named("openshift-create-cluster")
@SuppressWarnings("unused")
public class OpenshiftCreateClusterTask extends AWSWrite {
    private static final String[] INSTALL_CCOCTL = {"/bin/bash", "-c", "mkdir -p '/tmp/ccoctl' && wget -nv -O '/tmp/ccoctl/ccoctl-linux.tar.gz' 'https://mirror.openshift.com/pub/openshift-v4/x86_64/clients/ocp/latest/ccoctl-linux.tar.gz' && tar zxvf '/tmp/ccoctl/ccoctl-linux.tar.gz' -C '/tmp/ccoctl' && find /tmp/ccoctl/ && sudo mv '/tmp/ccoctl/ccoctl' '/usr/local/bin/' && rm '/tmp/oc/openshift-client-linux.tar.gz'"};
    private static final String[] INSTALL_OPENSHIFT_INSTALL = {"/bin/bash", "-c", "mkdir -p '/tmp/openshift-installer' && wget -nv -O '/tmp/openshift-installer/openshift-install-linux.tar.gz' 'https://mirror.openshift.com/pub/openshift-v4/x86_64/clients/ocp/latest/openshift-install-linux.tar.gz' && tar zxvf '/tmp/openshift-installer/openshift-install-linux.tar.gz' -C '/tmp/openshift-installer' && sudo mv  '/tmp/openshift-installer/openshift-install' '/usr/local/bin/' && rm '/tmp/openshift-installer/openshift-install-linux.tar.gz'"};
    @Inject
    Instance<CheckShellCommandExistsTask> checkCmd;

    @Inject
    Engine engine;

    enum ClusterProfile {
        aws_ipi_sts,
        aws_ipi_default
    }

    @Override
    public void apply() {
        debug("ocp-create-cluster");


        List<Input> inputs = List.of(OCPInput.clusterName,
                OCPInput.baseDomain,
                OCPInput.sshKey,
                OCPInput.pullSecret,
                OCPInput.awsRegion);

        List<Input> missing = new ArrayList<>();
        Map<Input, String> present = new HashMap<>();
        for(var input:inputs){
            var inputValue = input(input)
                    .or( () -> fromConfig(input));
            if(inputValue.isPresent()){
                present.put(input, inputValue.get().toString());
            }else{
                missing.add(input);
            }
        }
        debug("{} inputs, {} present, {} missing", inputs.size(), present.size(), missing.size());
        if (!missing.isEmpty()){
            error("[{}] inputs are missing: {}", missing.size(), missing);
            throw new ConfigurationNotFoundException(missing.stream().map(Object::toString).toList());
        }
        var clusterName = present.get(OCPInput.clusterName);
        var clusterDir = getClusterDir(clusterName);
        if (! FSUtils.isEmptyDir(clusterDir))
            throw fail("Cluster directory already exists {} ", clusterDir);
        var credsDir = FSUtils.resolve(clusterDir, "ccoctl-creds");
        var outputDir = FSUtils.resolve(clusterDir, "ccoctl-output");
        var clusterRegion = aws().getRegion().toString();
        var profile = inputAs(OCPInput.clusterProfile, ClusterProfile.class)
                .orElse(ClusterProfile.aws_ipi_default);
        checkCommands();
        var data = present.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().toString(),
                        Map.Entry::getValue
                ));
        preCreate(clusterName, clusterDir, credsDir, outputDir, profile, data);
        createCluster(clusterName, clusterDir);
        debug("ocp-create-cluster done");
    }

    /*
    private static final Map<Input, >> fromConfigMap = new HashMap<>(){{
        put(OCPInput.clusterName, c -> c.ocp().clusterName());
        put(OCPInput.baseDomain, c -> c.ocp().baseDomain());
        put(OCPInput.sshKey, c -> c.ocp().sshKey());
        put(OCPInput.pullSecret, c -> c.ocp().pullSecret());
        put(OCPAWSInputRegion, c -> c.ocp().awsRegion());
    }};
    */

    @Inject
    Inputs inputs;
    private Optional<String> fromConfig(Input input) {
        return inputs.fromConfig(input);
    }


    private Map<String, String> validateConfig(String clusterName) {

        var data = Map.of(
                "clusterName", Optional.of(clusterName),
                "baseDomain", getConfig().ocp().baseDomain(),
                "awsRegion", getConfig().ocp().awsRegion(),
                "pullSecret", getConfig().ocp().pullSecret(),
                "sshKey", getConfig().ocp().sshKey()
        );
        var entries = data.entrySet();
        var missing = new ArrayList<String>();
        var present = new HashMap<String, String>();
        for (var entry : entries) {
            var key = entry.getKey();
            var value = entry.getValue();
            if(value.isEmpty()) {
                missing.add(key);
            }else {
                present.put(key, value.get());
            }
        }
        if (! missing.isEmpty()){
            throw new ConfigurationNotFoundException(missing);
        }else {
            debug("All required configuration found");
            return present;
        }
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

    private void expectCapability(Capabilities capability) {
        if(! hasCapabilities(capability)){
            debug("Missing capability {} ", capability);
            throw new CapabilityNotFoundException(capability);
        }
    }

    protected boolean hasCapabilities(Capabilities... cs){
        return tasks().hasCapabilities(cs);
    }

    private void preCreate(String clusterName, Path clusterDir, Path credsDir, Path outputDir, ClusterProfile profile, Map<String, String> configData) {
        debug("Preparing to create cluster {} with profile {}", clusterName, profile);
        switch (profile){
            case aws_ipi_sts:
                createAllCcoctlResources(clusterName, credsDir, outputDir);
                break;
            case aws_ipi_default:
                break;
            default:
                throw fail("Unknown profile: {}", profile);
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
        var installConfigTemplate = engine.getTemplate(location);

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
                "--region="+getRegion().toString(),
                "--credentials-requests-dir="+credsDir.toString(),
                "--output-dir="+outputDir);

        if (ccoctlExec.isPresent()){
            logger().debug("ccoctl output: {}", ccoctlExec.get());
        }else{
            throw fail("ccoctl failed.");
        }
    }

    private void checkCommands() {
        checkCmd("ccoctl", INSTALL_CCOCTL);
        checkCmd("openshift-install", INSTALL_OPENSHIFT_INSTALL);
    }

    private void checkCmd(String executable, String[] installCmd) {
        var checkTask = withInput(checkCmd, ShellInput.cmd, executable);
        var installTask = shellTask(installCmd);
        retry(checkTask, installTask);
    }

}
