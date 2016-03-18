package be.cegeka.kafka.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class TimeSeriesKafkaProducer {

    public static final String TOPIC_NAME = "test";
    public static final String BOOTSTRAP_SERVERS = "192.168.99.100:9092";

    public static void main(String[] args) {
        try {
            Producer<String, String> producer = new KafkaProducer(getProducerProperties());

            for (int i = 0; i < 100; i++)
                producer.send(new ProducerRecord<>(TOPIC_NAME, Integer.toString(i), Integer.toString(i)));

            producer.close();
        } catch(Exception e) {
            System.out.print(e);
        }
    }

    private static Properties getProducerProperties() {
        Properties props = new Properties();
        props.put("bootstrap.servers", BOOTSTRAP_SERVERS);
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", StringSerializer.class.getName());
        return props;
    }

}
