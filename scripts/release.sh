#!/usr/bin/env bash
set -ex

mvn clean package

jpackage \
  --input target/quarkus-app \
  --dest target/dist \
  --name cloud-janitor \
  --main-jar quarkus-run.jar \
  --main-class cj.Main \
  --type app-image \
  --java-options '--enable-preview'

pushd target/dist/cloud-janitor
zip -r ../../cloud-janitor.zip .
popd

unzip -l target/cloud-janitor.zip

mvn jreleaser:full-release
echo "done"


# brew tap CaravanaCloud/homebrew-tap
# brew install cloud-janitor