#!/usr/bin/env bash

set -x

REPO="someowner/somerepo"

# create image update tag from current timestamp (format: test-app-tag_update_yyyy-MM-dd_HH-mm)
tagTimestamp=$(date '+%Y-%m-%d_%H_%M')
echo "Tag Timestamp: $tagTimestamp"
updateTag="test-app-tag_update_$tagTimestamp"

# build the application
# bash build.sh

appService="./application/src/main/java/tools/descartes/coffee/application/AppApplication.java"
sed -i "s/AppVersion\.V1/AppVersion\.V2/g" "$appService"

# build, tag and push docker image to docker hub repo
docker build -t "test-app:$updateTag" -f application/Dockerfile .
docker tag "test-app:$updateTag" "$REPO:$updateTag"
docker push "$REPO:$updateTag"

sed -i "s/AppVersion\.V2/AppVersion\.V1/g" "$appService"

# replace tag name in config file
configFile="./controller/src/main/resources/application.properties"
sed -i "s/\(updateImage=$REPO\):.*/\1:$updateTag/g" "$configFile"
set +x