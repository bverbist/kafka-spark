#!/bin/bash
set -e

if [ "$1" = 'kafka-server-start.sh' ]; then

    sed -i -r 's|zookeeper.connect=localhost:2181|zookeeper.connect=zookeeper:2181|g' /opt/kafka_2.10-0.9.0.1/config/server.properties

    exec "$@"
fi

exec "$@"