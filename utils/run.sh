#!/usr/bin/env bash

java -jar ../controller/target/controller-0.1-SNAPSHOT.jar &
echo "Controller starting, waiting 10 secs before startimg the test script..."
sleep 10s
curl localhost:8080/start
