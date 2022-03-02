#/bin/bash

DATESTAMP=$(date +%Y%m%d-%H%M%S)
TAG="tasktree-$DATESTAMP"
NOTES=${NOTES:-"Test release notes."}
git tag $TAG -m "$NOTES"
git push origin $TAG
