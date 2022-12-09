package cj.cloud;

import cj.StartupObserver;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@SuppressWarnings("unused")
public class CloudStartupObserver extends StartupObserver {
    @Override
    public void onStart(){
        // TODO: Support other cloud providers :)
        describeInput(CloudInputs.cloudProvider,
                "cloud.provider",
                null,
                () -> CloudProvider.aws);
        trace("Cloud startup observer completed.");
    }
}
