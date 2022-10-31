#!/usr/bin/env bash

set -x
bash build.sh
bash ./utils/docker-push-proxy.sh
bash ./utils/docker-push.sh
bash ./utils/docker-push-update.sh
mvn -f controller/pom.xml clean package spring-boot:repackage
set +x