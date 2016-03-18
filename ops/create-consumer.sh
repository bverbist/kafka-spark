echo "Topic:"
read $topic

docker run -it --rm --link ops_zookeeper_1:zookeeper --link ops_kafka-0_1:kafka dockerkafka/kafka kafka-console-consumer.sh --zookeeper zookeeper:2181 --topic $topic --from-beginning
