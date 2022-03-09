#!/bin/bash
export DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
export RDIR="$(dirname "$DIR")"
export TT_CMD="mvn -f $RDIR/tt-cli/pom.xml clean compile exec:java -Dexec.mainClass=tasktree.Main"

export QUARKUS_PROFILE="prod"
export TT_TASK="cleanup-aws"
export TT_DRYRUN="true"
export TT_OCP_BASEDIR="devcluster.openshift.com"
export TT_AWS_REGION="eu-west-1,ap-northeast-1"
export TT_AWS_CLEANUP_PREFIX="rhnb-"

echo "Running: $TT_CMD"
eval $TT_CMD
