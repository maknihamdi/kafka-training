package com.kafka.training.streams.config;

import com.kafka.training.common.model.EnrichedEvent;
import com.kafka.training.common.model.Event;
import com.kafka.training.common.model.UserProfile;
import com.kafka.training.common.serde.JsonSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaStreamsConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.streams.application-id}")
    private String applicationId;

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration kStreamsConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        // Pour voir la topologie dans les logs
        props.put(StreamsConfig.TOPOLOGY_OPTIMIZATION_CONFIG, StreamsConfig.OPTIMIZE);

        return new KafkaStreamsConfiguration(props);
    }

    @Bean
    public JsonSerde<Event> eventSerde() {
        return new JsonSerde<>(Event.class);
    }

    @Bean
    public JsonSerde<UserProfile> userProfileSerde() {
        return new JsonSerde<>(UserProfile.class);
    }

    @Bean
    public JsonSerde<EnrichedEvent> enrichedEventSerde() {
        return new JsonSerde<>(EnrichedEvent.class);
    }
}
