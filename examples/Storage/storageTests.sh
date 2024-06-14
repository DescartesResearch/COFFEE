#!/usr/bin/env bash

# TODO: Measure without volume

declare -a STORAGECLASSES=("NULL" "pd-csi-balanced" "pd-csi-standard" "pd-csi-ssd" "filestore-standard" "filestore-premium")
REPS=30

for c in "${STORAGECLASSES[@]}"
do
  # Write storage class in config file
  sed -i "s/^kubernetes.storageClassName=.*$/kubernetes.storageClassName=$c/g" ../controller/src/main/resources/application.properties
  # rebuild
  bash buildAndPush.sh
  i=0
  while [ $i -le $REPS ]
  do
    echo "$c run $i"
    bash runBlocking.sh
    docker exec -it database-my_sql-1 bash -c 'mysql -h localhost -u user -pOF-Benchmarking2022 --database of_monitor_db --batch -e "select * from StorageTime"'  > sed 's/\t/","/g;s/^/"/;s/$/"/;s/\n//' > "$c-$i-storage.csv"
    docker exec -it database-my_sql-1 bash -c 'mysql -h localhost -u user -pOF-Benchmarking2022 --database of_monitor_db --batch -e "select * from CommandExecutionTime"'  > sed 's/\t/","/g;s/^/"/;s/$/"/;s/\n//' > "$c-$i-start-remove.csv"
    i=$(( $i + 1 ))
    sleep 30
  done
done
