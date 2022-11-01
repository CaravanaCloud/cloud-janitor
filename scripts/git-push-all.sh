#!/bin/bash
set -ex

DATESTAMP=$(date +%Y%m%d-%H%M%S)

git add .
git commit --allow-empty -m "WIP at $DATESTAMP"
git push
