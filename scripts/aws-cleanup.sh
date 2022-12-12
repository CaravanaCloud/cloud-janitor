#!/bin/bash
set -ex

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

CJ_IMAGE="docker.io/caravanacloud/cloud-janitor:latest"
docker run \

  $CJ_IMAGE
echo done