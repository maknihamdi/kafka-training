package com.kafka.training.consumer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kafka.training.consumer.model.Message;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.Properties;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public KafkaConsumer<String, Message> kafkaConsumer() {
        Properties props = new Properties();

        // Configuration du broker
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // Désérialisation des clés
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());

        // IMPORTANT: Consumer STANDALONE - Pas de group.id
        // On va utiliser assign() au lieu de subscribe()

        // Créer le JsonDeserializer avec la configuration appropriée
        JsonDeserializer<Message> jsonDeserializer = new JsonDeserializer<>(Message.class, objectMapper());
        jsonDeserializer.addTrustedPackages("*"); // Autoriser tous les packages
        jsonDeserializer.setUseTypeHeaders(false); // Ne pas utiliser les headers de type

        return new KafkaConsumer<>(props, new StringDeserializer(), jsonDeserializer);
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}
