echo "Topic:"
read topic

docker run -it --rm --link ops_kafka_1:kafka dockerkafka/kafka kafka-console-producer.sh --broker-list kafka:9092 --topic $topic
