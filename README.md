# kafka-spark
explore apache kafka and spark streaming

Code was just used during a hackathon, so do not expect to find a working setup here!

## To set up a Kafka cluster:
cd ops
docker-compose up -d

## To create a topic
cd ops
./create-topic.sh
Provide input for topic name, no. of partitions and replication factor

## manager
http://192.168.99.100:9000/

## console consumer

kafka-console-consumer.sh --zookeeper 192.168.99.100:2181 --topic test --from-beginning
