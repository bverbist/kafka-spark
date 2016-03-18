echo "Topic name:"
read topic

echo "No. of partitions:"
read partitions

echo "Replication factor:"
read replication

docker run -it --rm --link kafkadocker_zookeeper_1:zookeeper dockerkafka/kafka kafka-topics.sh --create --zookeeper zookeeper:2181 --replication-factor $replication --partitions $partitions --topic $topic

