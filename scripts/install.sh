#!/bin/bash
set -e -o xtrace
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
REPO_DIR="$DIR/.."

mvn clean package

echo "=== APP IMAGE === "
mkdir -p "target/jpackage/appimage"
jpackage -t "app-image" \
  --input "$REPO_DIR/target/binary/lib/" \
  --name "cloud-janitor" \
  --main-jar "cloudjanitor-1.0.0-SNAPSHOT.jar" \
  --main-class "cloudjanitor.Main" \
  --java-options "--enable-preview" \
  --app-version "1.0.0-SNAPSHOT" \
  --dest "$REPO_DIR/target/jpackage/appimage" \
  --verbose

echo "=== RPM === "
mkdir -p "target/jpackage/rpm"
jpackage -t "rpm" \
  --input "$REPO_DIR/target/binary/lib/" \
  --name "cloud-janitor" \
  --main-jar "cloudjanitor-1.0.0-SNAPSHOT.jar" \
  --main-class "cloudjanitor.Main" \
  --java-options "--enable-preview" \
  --app-version "1.0.0-SNAPSHOT" \
  --dest "$REPO_DIR/target/jpackage/appimage" \
  --verbose


