package be.cegeka.kafka.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class TimeSeriesKafkaProducer {

    public static final String TOPIC_NAME = "test";

    public static void main(String[] args) {
        try {
            Producer<String, String> producer = new KafkaProducer(getProducerProperties());

            for (int i = 0; i < 100; i++)
                producer.send(new ProducerRecord<String, String>(TOPIC_NAME, Integer.toString(i), Integer.toString(i)));

            producer.close();
        } catch(Exception e) {
            System.out.print(e);
        }
    }

    private static Properties getProducerProperties() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "192.168.99.100:32800");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        return props;
    }

}
