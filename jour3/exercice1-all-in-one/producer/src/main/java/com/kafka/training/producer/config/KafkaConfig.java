package com.kafka.training.producer.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // ========================================
    // Kafka Admin Configuration
    // ========================================

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    // Topic pour les événements utilisateurs
    @Bean
    public NewTopic userEventsTopic() {
        return TopicBuilder.name("user-events")
                .partitions(3)
                .replicas(1)
                .build();
    }

    // Topic compacté pour les profils utilisateurs (données de référence)
    @Bean
    public NewTopic userProfilesTopic() {
        return TopicBuilder.name("user-profiles")
                .partitions(3)
                .replicas(1)
                .compact() // Topic compacté pour KTable
                .build();
    }

    // Topic de sortie pour les événements filtrés (créé par Kafka Streams)
    @Bean
    public NewTopic filteredEventsTopic() {
        return TopicBuilder.name("filtered-events")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic enrichedEventsTopic() {
        return TopicBuilder.name("enriched-events")
                .partitions(3)
                .replicas(1)
                .build();
    }


    // ========================================
    // Producer Configuration
    // ========================================

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
