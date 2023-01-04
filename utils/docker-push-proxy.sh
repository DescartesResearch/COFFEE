#!/usr/bin/env bash

set -x

REPO=$1

# create image proxy tag from current timestamp (format: test-app-tag_proxy_yyyy-MM-dd_HH-mm)
tagTimestamp=$(date '+%Y-%m-%d_%H_%M')
echo "Tag Timestamp: $tagTimestamp"
proxyTag="test-app-tag_proxy_$tagTimestamp"

# build the application
# bash build.sh

# build, tag and push docker image to docker hub repo
docker build -t "test-app:$proxyTag" -f ../proxy/Dockerfile ..
docker tag "test-app:$proxyTag" "$REPO:$proxyTag"
docker push "$REPO:$proxyTag"

# replace tag name in config file
configFile="../controller/src/main/resources/application.properties"
sed -i "s~\(proxyImage=$REPO\):.*~\1:$proxyTag~g" "$configFile"
set +x