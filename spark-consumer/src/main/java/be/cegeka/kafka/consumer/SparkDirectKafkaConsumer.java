package be.cegeka.kafka.consumer;

import java.util.*;
import java.util.regex.Pattern;

import scala.Tuple2;

import kafka.serializer.StringDecoder;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.*;
import org.apache.spark.streaming.api.java.*;
import org.apache.spark.streaming.kafka.KafkaUtils;
import org.apache.spark.streaming.Durations;

import static java.util.Arrays.asList;

public class SparkDirectKafkaConsumer {

    private static final int BATCH_INTERVAL_IN_SECONDS = 2;
    private static final Pattern SPACE = Pattern.compile(" ");

    private static final List KAFKA_TOPICS = asList("test");
    //private static final String KAFKA_BROKERS = "192.168.99.100:9092"; //comma separated
    private static final String KAFKA_SERVERS = "192.168.99.100:9090,192.168.99.100:9091,192.168.99.100:9092"; //comma separated

    public static void main(String[] args) {
        JavaStreamingContext jssc = createStreamingContext();

        HashMap<String, String> kafkaParams = new HashMap<>();
//        kafkaParams.put("metadata.broker.list", KAFKA_BROKERS);
        kafkaParams.put("bootstrap.servers", KAFKA_SERVERS);

        HashSet<String> topicsSet = new HashSet<>(KAFKA_TOPICS);

        JavaPairInputDStream<String, String> messages = createDirectKafkaStream(jssc, kafkaParams, topicsSet);

        configureSparkMagic(messages);

        jssc.start();
        jssc.awaitTermination();
    }

    private static JavaStreamingContext createStreamingContext() {
        SparkConf sparkConf = new SparkConf()
                .setAppName("SparkDirectKafkaConsumer")
                .setMaster("spark://myhost:7077");
//                .setMaster("local[4]"); //4 threads
        return new JavaStreamingContext(sparkConf, Durations.seconds(BATCH_INTERVAL_IN_SECONDS));
    }

    private static JavaPairInputDStream<String, String> createDirectKafkaStream(JavaStreamingContext jssc, HashMap<String, String> kafkaParams, HashSet<String> topicsSet) {
        Class<String> keyClass = String.class;
        Class<String> valueClass = String.class;
        Class<StringDecoder> keyDecoderClass = StringDecoder.class;
        Class<StringDecoder> valueDecoderClass = StringDecoder.class;

        return KafkaUtils.createDirectStream(
                jssc,
                keyClass,
                valueClass,
                keyDecoderClass,
                valueDecoderClass,
                kafkaParams,
                topicsSet
        );
    }

    private static void configureSparkMagic(JavaPairInputDStream<String, String> messages) {
        // Get the lines, split them into words, count the words and print

        JavaDStream<String> lines = messages.map((Function<Tuple2<String, String>, String>) tuple2 -> tuple2._2());
        JavaDStream<String> words = lines.flatMap((FlatMapFunction<String, String>) x -> asList(SPACE.split(x)));
        JavaPairDStream<String, Integer> wordCounts = words.mapToPair(
                new PairFunction<String, String, Integer>() {
                    @Override
                    public Tuple2<String, Integer> call(String s) {
                        return new Tuple2<>(s, 1);
                    }
                }).reduceByKey(
                new Function2<Integer, Integer, Integer>() {
                    @Override
                    public Integer call(Integer i1, Integer i2) {
                        return i1 + i2;
                    }
                });
        wordCounts.print();
    }

}
