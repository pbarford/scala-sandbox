#!/bin/bash


if [[ -z "$START_TIMEOUT" ]]; then
    START_TIMEOUT=600
fi

start_timeout_exceeded=false
count=0
step=10
while netstat -lnt | awk '$4 ~ /:'$KAFKA_PORT'$/ {exit 1}'; do
    echo "waiting for kafka to be ready"
    sleep $step;
    count=$(expr $count + $step)
    if [ $count -gt $START_TIMEOUT ]; then
        start_timeout_exceeded=true
        break
    fi
done

if $start_timeout_exceeded; then
    echo "Not able to auto-create topic (waited for $START_TIMEOUT sec)"
    exit 1
fi

if [[ -n $KAFKA_CREATE_TOPICS ]]; then
    IFS=','; for topicToCreate in $KAFKA_CREATE_TOPICS; do
        echo "creating topic: $topicToCreate"
        IFS=':' read -a topicConfig <<< "$topicToCreate"
        echo $KAFKA_ZOOKEEPER_CONNECT
        echo "creating topic: ${topicConfig[0]}, partitions: ${topicConfig[1]}, replication-factor: ${topicConfig[2]}"
        if [ ${topicConfig[3]} ]; then
          JMX_PORT=''
          $KAFKA_HOME/bin/kafka-topics.sh --create --zookeeper $KAFKA_ZOOKEEPER_CONNECT --partitions ${topicConfig[1]} --replication-factor ${topicConfig[2]} --topic "${topicConfig[0]}" --config cleanup.policy="${topicConfig[3]}"
        else
          JMX_PORT=''
          $KAFKA_HOME/bin/kafka-topics.sh --create --zookeeper $KAFKA_ZOOKEEPER_CONNECT --partitions ${topicConfig[1]} --replication-factor ${topicConfig[2]} --topic "${topicConfig[0]}"
        fi
    done
fi
