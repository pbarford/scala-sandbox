#!/bin/bash
export JAVA_HOME=/usr/java/jdk1.8.0_141
export PATH=$JAVA_HOME/bin:$PATH

rm -f /opt/cassandra/logs/system.log
touch /opt/cassandra/logs/system.log

ip_addr="$(ifconfig | grep -Eo 'inet (addr:)?([0-9]*\.){3}[0-9]*' | grep -Eo '([0-9]*\.){3}[0-9]*' | grep -v '127.0.0.1')"

/opt/cassandra/bin/cassandra

while true; do
    finished="$(grep -c ' Starting up server gossip' /opt/cassandra/logs/system.log)"
    if [ $finished -eq 1 ]; then
            break;
    fi
done
echo 'Sleeping 5 seconds'
sleep 5
echo '*********************'
echo '   CONFIGURING C*'
echo '*********************'
/opt/cassandra/bin/cqlsh -f /data/drop-keyspace.cql
echo 'Sleeping 5 seconds'
sleep 5
/opt/cassandra/bin/cqlsh -f /data/create-keyspace.cql
echo '*********************'
echo '    C* CONFIGURED '
echo '*********************'
