package cj.ocp;

enum ClusterProfile {
        sts(true),
        standard(false);

        final boolean ccoctl;

        ClusterProfile(boolean ccoctl) {
            this.ccoctl = ccoctl;
        }
    }