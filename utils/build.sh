#!/usr/bin/env bash

set -x
mvn -f ../shared/pom.xml clean install
mvn -f ../application/pom.xml clean package spring-boot:repackage
mvn -f ../controller/pom.xml clean package spring-boot:repackage
mvn -f ../proxy/pom.xml clean package spring-boot:repackage
set +x