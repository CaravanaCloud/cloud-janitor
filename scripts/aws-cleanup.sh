#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PDIR="$(dirname "$DIR")"

"${PDIR}/mvnw" clean compile exec:java \
  -Dexec.mainClass='tasktree.Main' \
  -Dquarkus.profile=awscleanup
