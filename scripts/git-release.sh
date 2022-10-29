#!/bin/bash
set -ex

MAJOR=1
MINOR=2
PATCH=$(date +%Y%m%d%H%M%S)
VERSION="$MAJOR.$MINOR.$PATCH"
TAG="v$VERSION"
NOTES=${NOTES:-"cloud-janitor release $VERSION"}
git tag $TAG -m "$NOTES"
git push origin $TAG
