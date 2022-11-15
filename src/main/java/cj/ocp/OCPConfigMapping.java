package cj.ocp;

import cj.Inputs;
import io.quarkus.runtime.StartupEvent;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

public class OCPConfigMapping {
    @Inject
    Inputs inputs;

    public void onStart(@Observes StartupEvent ev){
        System.out.println("Mapping OCP inputs to config");
        inputs.fromConfigFn(OCPInput.clusterName, c-> c.ocp().clusterName())
            .fromConfigFn(OCPInput.baseDomain, c-> c.ocp().baseDomain())
            .fromConfigFn(OCPInput.sshKey, c-> c.ocp().sshKey())
            .fromConfigFn(OCPInput.pullSecret, c-> c.ocp().pullSecret())
            .fromConfigFn(OCPInput.awsRegion, c-> c.ocp().awsRegion());

    }
}
