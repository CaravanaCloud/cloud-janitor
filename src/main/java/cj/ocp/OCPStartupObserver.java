package cj.ocp;

import cj.Inputs;
import cj.Tasks;
import io.quarkus.runtime.StartupEvent;
import org.slf4j.Logger;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import static cj.ocp.OCPInput.*;

@SuppressWarnings("unused")
public class OCPStartupObserver {
    @Inject
    Inputs inputs;

    @Inject
    Tasks tasks;

    @Inject
    Logger logger;

    public void onStart(@Observes StartupEvent ev){
        inputs.putConfig(clusterName,
                        "cj.ocp.clusterName",
                        c -> c.ocp().clusterName(),
                        () -> tasks.getExecutionId())
            .putConfig(baseDomain,
                    "cj.ocp.baseDomain",
                    c -> c.ocp().baseDomain(),
                    null)
            .putConfig(sshKey,
                    "cj.ocp.sshKey",
                    c -> c.ocp().sshKey(),
                    null)
            .putConfig(pullSecret,
                    "cj.ocp.pullSecret",
                    c -> c.ocp().pullSecret(),
                    null)
            .putConfig(awsRegion,
                    "cj.ocp.awsRegion",
                    c -> c.ocp().awsRegion(),
                    null)
                .putConfig(clusterProfile,
                        "cj.ocp.clusterProfile",
                        c -> c.ocp().clusterProfile(),
                        () -> ClusterProfile.aws_ipi_default)
                .putConfig(instanceType,
                        "cj.ocp.instanceType",
                        c -> c.ocp().instanceType(),
                        () -> "m5.xlarge");

        logger.debug("OpenShift config mapping complete.");
    }
}
