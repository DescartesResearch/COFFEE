#!/usr/bin/env bash

set -x

REPO=$1

# create image tag from current timestamp (format: test-app-tag_yyyy-MM-dd_HH-mm)
tagTimestamp=$(date '+%Y-%m-%d_%H_%M')
echo "Tag Timestamp: $tagTimestamp"

# build the application
# bash build.sh

# build, tag and push docker image to docker hub repo
docker build -t "test-app:$tagTimestamp" -f ../application/Dockerfile ..
docker tag "test-app:$tagTimestamp" "$REPO:$tagTimestamp"
docker push "$REPO:$tagTimestamp"

# replace tag name in config file
configFile="../controller/src/main/resources/application.properties"
sed -i "s~\(appImage=$REPO\):.*~\1:$tagTimestamp~g" "$configFile"
set +x
