package cj.ocp;

import cj.Inputs;
import cj.Tasks;
import io.quarkus.runtime.StartupEvent;


import javax.enterprise.event.Observes;
import javax.inject.Inject;

import static cj.ocp.OCPInput.*;

public class OCPConfigMapping {
    @Inject
    Inputs inputs;

    @Inject
    Tasks tasks;

    public void onStart(@Observes StartupEvent ev){
        System.out.println("Mapping OCP inputs to config");

        inputs.putConfig(clusterName,
                        "cj.ocp.clusterName",
                        c -> c.ocp().clusterName(),
                        () -> tasks.getExecutionId())
            .putConfig(baseDomain,
                    "cj.ocp.baseDomain",
                    c -> c.ocp().baseDomain(),
                    null)
            .putConfig(sshKey,
                    "cj.ocp.baseDomain",
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
    }
}
