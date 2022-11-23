#!/usr/bin/env bash
set -ex

## expect cloudProfider=aws
## expect ocpInfrastructureProvider=ipi
## expect ocpClusterProfile=default
## expect ocpClusterDir

echo "openshift-install create cluster --dir=${clusterDir} --log-level=debug"
echo "done openshift-install"
