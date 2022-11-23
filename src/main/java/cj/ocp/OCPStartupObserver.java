package cj.ocp;

import cj.StartupObserver;

import static cj.ocp.OCPInput.*;

@SuppressWarnings("unused")
public class OCPStartupObserver extends StartupObserver {
    @Override
    public void onStart(){
        putConfig(clusterName,
                        "cj.ocp.clusterName",
                        c -> c.ocp().clusterName(),
                        () -> "cj"+System.currentTimeMillis());
        putConfig(baseDomain,
                    "cj.ocp.baseDomain",
                    c -> c.ocp().baseDomain(),
                    null);
        putConfig(sshKey,
                    "cj.ocp.sshKey",
                    c -> c.ocp().sshKey(),
                    null);
        putConfig(pullSecret,
                    "cj.ocp.pullSecret",
                    c -> c.ocp().pullSecret(),
                    null);
        putConfig(awsRegion,
                    "cj.ocp.awsRegion",
                    c -> c.ocp().awsRegion(),
                    null);
        putConfig(clusterProfile,
                        "cj.ocp.clusterProfile",
                        c -> c.ocp().clusterProfile(),
                        () -> ClusterProfile.standard);
        putConfig(instanceType,
                        "cj.ocp.instanceType",
                        c -> c.ocp().instanceType(),
                        () -> "m5.xlarge");
        putConfig(infrastructureProvider,
                        "cj.ocp.infrastructureProvider",
                        null,
                        () -> OCPInfrastructureProvider.ipi);
        trace("OpenShift startup completed.");
    }
}
