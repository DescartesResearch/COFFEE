#!/usr/bin/env bash

set -x
EXPERIMENT_COUNTER=1
while [ $EXPERIMENT_COUNTER -le 10 ]
do
  echo "Run $EXPERIMENT_COUNTER / 10"
  docker compose -f ./database/docker-compose.yaml up -d
  sleep 10
  bash run.sh &
  PID=$!
  sleep 20
  curl localhost:8080/start
  wait $PID
  docker exec -it database-my_sql-1 bash -c 'mysql -h localhost -u user -pOF-Benchmarking2022 --database of_monitor_db --batch -e "select * from CommandExecutionTime"'  | sed 's/\t/,/g;' > "./results/StartRemove$EXPERIMENT_COUNTER.csv"
  docker compose -f ./database/docker-compose.yaml down
  cp -r /var/lib/docker/volumes/database_cobench_mysql_volume "./results/StartRemove$EXPERIMENT_COUNTER"
  cp test-sequence.script "./results/StartRemove$EXPERIMENT_COUNTER"
  EXPERIMENT_COUNTER=$((EXPERIMENT_COUNTER+1))
  sed -i "s/start.*/start $EXPERIMENT_COUNTER/g" test-sequence.script
  sed -i "s/remove.*/remove $EXPERIMENT_COUNTER/g" test-sequence.script
done
set +x