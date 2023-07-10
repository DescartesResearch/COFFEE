#!/usr/bin/env bash

set -x

REPOSITORY="descartespro/loadgen-tests"

bash build.sh
bash docker-push-proxy.sh $REPOSITORY
bash docker-push.sh $REPOSITORY
bash docker-push-update.sh $REPOSITORY
mvn -f ../controller/pom.xml clean package spring-boot:repackage
set +x