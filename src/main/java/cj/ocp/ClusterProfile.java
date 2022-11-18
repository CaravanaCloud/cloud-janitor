package cj.ocp;

enum ClusterProfile {
        aws_ipi_sts(true),
        aws_ipi_default(false);

        final boolean ccoctl;

        ClusterProfile(boolean ccoctl) {
            this.ccoctl = ccoctl;
        }
    }