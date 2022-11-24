package cj.ocp;

enum ClusterProfile {
        singleNode(false),

        sts(true),
        standard(false);

        final boolean ccoctl;

        ClusterProfile(boolean ccoctl) {
            this.ccoctl = ccoctl;
        }
    }