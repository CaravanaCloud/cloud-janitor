#!/bin/bash
export QUARKUS_PROFILE="prod"

export TT_TASK="create-trail-table"
export TT_DRYRUN="false"

export TT_CMD="mvn clean compile exec:java -Dexec.mainClass=tasktree.Main"
echo "Running: $TT_CMD"
eval $TT_CMD
