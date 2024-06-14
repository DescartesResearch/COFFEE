#!/usr/bin/env bash

java -jar ../controller/target/controller-0.1-SNAPSHOT.jar &
JOBID=$(jobs | tail -n1 | cut -d' ' -f1 | tr -d '][+')
echo "Controller starting, waiting 20 secs before startimg the test script..."
sleep 20s
curl localhost:8080/start
fg %"$JOBID"