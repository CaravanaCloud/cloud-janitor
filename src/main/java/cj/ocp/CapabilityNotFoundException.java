package cj.ocp;

import cj.Capabilities;

public class CapabilityNotFoundException extends RuntimeException {
    private final Capabilities capability;

    public CapabilityNotFoundException(Capabilities capability) {
        this.capability = capability;
    }

    public Capabilities getCapability() {
        return capability;
    }
}
