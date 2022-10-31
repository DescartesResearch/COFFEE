#!/usr/bin/env bash

docker compose up -d

# docker run --name co-mysql \
#  -e MYSQL_ROOT_PASSWORD=password \
#  -e MYSQL_USER=user \
#  -e MYSQL_PASSWORD=OF-Benchmarking2022 \
#  -e MYSQL_DATABASE=of_monitor_db \
#  -p 3306:3306 \
#  -d mysql:5.7.39
