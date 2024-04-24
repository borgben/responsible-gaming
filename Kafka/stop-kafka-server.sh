#/bin/bash

./kafka/bin/kafka-server-stop.sh &
rm -rf /tmp/kafka-logs /tmp/zookeeper

