#/bin/bash

DATESTAMP=$(date +%Y%m%d-%H%M%S)
TAG="tasktree-$DATESTAMP"
NOTES=${NOTES:-"Test release notes."}
gh release create "$TAG" --notes "$NOTES"
