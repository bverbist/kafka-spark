docker run -it --rm --link ops_zookeeper_1:zookeeper dockerkafka/kafka kafka-topics.sh --list --zookeeper zookeeper:2181
