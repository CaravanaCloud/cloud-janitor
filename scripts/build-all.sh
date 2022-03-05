#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
RDIR="$DIR/.."

mvn clean package -Dquarkus.package.type=uber-jar

jpackage --input "$RDIR/tt-cli/target" \
  --main-jar "tasktree-runner.jar" \
  --name "tasktree" \
  --main-class "tasktree.Main" \
  --type "rpm" \
  --java-options "--enable-preview" \
  --app-version "0.1.$(date +%s)" \
  --linux-app-release "1" \
  --description "TaskTree RPM Package" \
  --vendor "CaravanaCloud" \
  --verbose

echo "done"
