#/bin/bash

MAJOR=0
MINOR=1
PATCH=$(date +%Y%m%d%H%M%S)
VERSION="$MAJOR.$MINOR.$PATCH"
TAG="tasktree-$VERSION"
NOTES=${NOTES:-"TaskTree release $VERSION"}
git tag $TAG -m "$NOTES"
git push origin $TAG
