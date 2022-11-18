package cj.ocp;

import cj.Inputs;
import cj.Tasks;
import io.quarkus.runtime.StartupEvent;
import org.slf4j.Logger;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import static cj.ocp.OCPInput.*;

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
                    null);
        logger.debug("OpenShift config mapping complete.");
    }
}
