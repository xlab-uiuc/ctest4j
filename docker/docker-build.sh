#!/bin/bash

RUNNER_APP_DIR=$1
RUNNER_BRANCH=$2
TAG=$3

# if RUNNER_APP_DIR is not set, return error
if [ -z "$RUNNER_APP_DIR" ]; then
  echo "Please set RUNNER_APP_DIR"
  exit 1
fi

# if RUNNER_BRANCH is not set, send info
if [ -z "$RUNNER_BRANCH" ]; then
  echo "Please set RUNNER_BRANCH"
  exit 1
fi

if [ -z "$TAG" ]; then
  echo "Please set TAG"
  exit 1
fi

# if RUNNER_APP_DIR is not exist or not a directory, return error
if [ ! -d "$RUNNER_APP_DIR" ]; then
  echo "${RUNNER_APP_DIR} is not a directory"
  exit 1
fi

docker build --build-arg RUNNER_APP_DIR=${RUNNER_APP_DIR} --build-arg RUNNER_BRANCH=${RUNNER_BRANCH} --no-cache -t shuaiwang516/runner-image:${TAG} -f Dockerfile .
