package be.cegeka.kafka.consumer.spring;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.SourcePollingChannelAdapterSpec;
import org.springframework.integration.dsl.kafka.Kafka;
import org.springframework.integration.dsl.kafka.KafkaHighLevelConsumerMessageSourceSpec;
import org.springframework.integration.dsl.kafka.KafkaProducerMessageHandlerSpec;
import org.springframework.integration.dsl.support.Consumer;
import org.springframework.integration.kafka.support.ZookeeperConnect;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@EnableIntegration
@SpringBootApplication
public class Application {

//    public static final String TEST_TOPIC_ID = "test";

    @Component
    public static class NetflixKafkaConfig extends KafkaConfig {

        protected NetflixKafkaConfig() {
            super("netflix");
        }
    }

    @Component
    public static class TestKafkaConfig extends KafkaConfig {

        protected TestKafkaConfig() {
            super("test");
        }
    }

    public static abstract class KafkaConfig {

//        @Value("${kafka.topic:" + TEST_TOPIC_ID + "}")
        private final String topic;

//        @Value("${kafka.address:localhost:9092}")
        private String brokerAddress = "192.168.99.100:9092";

//        @Value("${zookeeper.address:localhost:2181}")
        private String zookeeperAddress = "192.168.99.100:2181";

        protected KafkaConfig(String topic) {
            this.topic = topic;
        }

        public String getTopic() {
            return topic;
        }

        public String getBrokerAddress() {
            return brokerAddress;
        }

        public String getZookeeperAddress() {
            return zookeeperAddress;
        }
    }

    @Configuration
    public static class ProducerConfiguration {

        @Autowired
        private NetflixKafkaConfig netflixKafkaConfig;
        @Autowired
        private TestKafkaConfig testKafkaConfig;
        @Autowired
        private NetflixDataReader netflixDataReader;

        private static final String OUTBOUND_ID = "outbound";
        private static final String OUTBOUND_NETFLIX = "outboundNetflix";

        private Log log = LogFactory.getLog(getClass());

//        @Bean
//        @DependsOn(OUTBOUND_ID)
//        CommandLineRunner kickOff(
//                @Qualifier(OUTBOUND_ID + ".input") MessageChannel in) {
//            return args -> {
//                for (int i = 0; i < 1000; i++) {
//                    in.send(new GenericMessage<>("#" + i));
//                    log.info("sending message #" + i);
//                }
//            };
//        }

        @Bean
        @DependsOn(OUTBOUND_NETFLIX)
        CommandLineRunner netflixData(
                @Qualifier(OUTBOUND_NETFLIX + ".input") MessageChannel in) {
            return args -> {
                netflixDataReader.streamMovieRatings()
                        .forEach(movieRating -> {
                            in.send(new GenericMessage<>(movieRating.toString()));
                        });
            };
        }

//        @Bean(name = OUTBOUND_ID)
//        IntegrationFlow testProducer() {
//            log.info("starting producer flow..");
//            return kafkaProducer(this.testKafkaConfig);
//        }

        @Bean(name = OUTBOUND_NETFLIX)
        IntegrationFlow netflixProducer() {
            log.info("starting producer flow..");
            return kafkaProducer(this.netflixKafkaConfig);
        }

        private IntegrationFlow kafkaProducer(KafkaConfig kafkaConfig) {
            return flowDefinition -> {

                Consumer<KafkaProducerMessageHandlerSpec.ProducerMetadataSpec> spec =
                        (KafkaProducerMessageHandlerSpec.ProducerMetadataSpec metadata)->
                                metadata.async(true)
                                        .batchNumMessages(10)
                                        .valueClassType(String.class)
                                        .<String>valueEncoder(String::getBytes);

                KafkaProducerMessageHandlerSpec messageHandlerSpec =
                        Kafka.outboundChannelAdapter(
                                props -> props.put("queue.buffering.max.ms", "15000"))
                                .messageKey(m -> m.getHeaders().get(IntegrationMessageHeaderAccessor.SEQUENCE_NUMBER))
                                .addProducer(kafkaConfig.getTopic(),
                                        kafkaConfig.getBrokerAddress(), spec);
                flowDefinition
                        .handle(messageHandlerSpec);
            };
        }
    }

    @Configuration
    public static class ConsumerConfiguration {

        @Autowired
        private TestKafkaConfig testKafkaConfig;

        @Autowired
        private NetflixKafkaConfig netflixKafkaConfig;

        private Log log = LogFactory.getLog(getClass());

//        @Bean
//        IntegrationFlow testConsumer() {
//            log.info("starting test consumer..");
//            return kafkaConsumer(testKafkaConfig, e -> log.info(e.getKey() + '=' + e.getValue()));
//        }

        @Bean
        IntegrationFlow netflixConsumer() {
            log.info("starting netflix consumer..");
            return kafkaConsumer(netflixKafkaConfig, e -> log.info(e.getKey() + '=' + e.getValue()));
        }

        private IntegrationFlow kafkaConsumer(KafkaConfig kafkaConfig, java.util.function.Consumer<Map.Entry<String, List<String>>> entryConsumer) {
            KafkaHighLevelConsumerMessageSourceSpec messageSourceSpec = Kafka.inboundChannelAdapter(
                    new ZookeeperConnect(kafkaConfig.getZookeeperAddress()))
                    .consumerProperties(props ->
                            props.put("auto.offset.reset", "smallest")
                                    .put("auto.commit.interval.ms", "100"))
                    .addConsumer("myGroup", metadata -> metadata.consumerTimeout(100)
                            .topicStreamMap(m -> m.put(kafkaConfig.getTopic(), 1))
                            .maxMessages(10)
                            .valueDecoder(String::new));

            Consumer<SourcePollingChannelAdapterSpec> endpointConfigurer = e -> e.poller(p -> p.fixedDelay(100));

            return IntegrationFlows
                    .from(messageSourceSpec, endpointConfigurer)
                    .<Map<String, List<String>>>handle((payload, headers) -> {
                        payload.entrySet().forEach(entryConsumer);
                        return null;
                    })
                    .get();
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}