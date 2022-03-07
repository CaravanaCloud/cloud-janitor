#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
RDIR="$DIR/.."

TIMESTAMP=$(date +%Y%m%d%H%M%S)
VERSION=0.1
RELEASE=$TIMESTAMP
ARCH=x86_64
PKG_NAME="tasktree"
PKG_DESCRIPTION="TaskTree RPM Package"
PKG_VENDOR="Caravana Cloud"
PKG_TYPE="rpm"
RPM_NAME="tasktree-cli.rpm"
RPM_PKG="./tasktree-$VERSION-$RELEASE.$ARCH.rpm"
IN_DIR="$RDIR/tt-cli/target"
OUT_DIR="$RDIR/tt-cli/target"
MAIN_JAR="tasktree-runner.jar"
MAIN_CLASS="tasktree.Main"
JAVA_OPTIONS="--enable-preview"


MVN_ARGS="-Dquarkus.package.type=uber-jar"
mvn clean package "$MVN_ARGS"

jpackage \
  --input "$IN_DIR" \
  --dest "$OUT_DIR" \
  --main-jar "$MAIN_JAR" \
  --name "$PKG_NAME" \
  --main-class "$MAIN_CLASS" \
  --type "$PKG_TYPE" \
  --java-options "$JAVA_OPTIONS" \
  --app-version "$VERSION" \
  --linux-app-release "$RELEASE" \
  --description "$PKG_DESCRIPTION" \
  --vendor "$PKG_VENDOR" \
  --verbose

cp "$OUT_DIR/$RPM_PKG" "$OUT_DIR/$RPM_NAME"

echo "installing new rpm"
echo "sudo rpm -Uvh --force tt-cli/target/tasktree-cli.rpm"  | tee >(xsel -ib)
sudo rpm -Uvh --force tt-cli/target/tasktree-cli.rpm
echo "Linking to system path"
echo "sudo ln -sf /opt/tasktree/bin/tasktree /usr/local/bin/tt"
sudo ln -sf /opt/tasktree/bin/tasktree /usr/local/bin/tt
echo "done. Try this:"
echo "tt marvin" | tee >(xsel -ib)
echo
