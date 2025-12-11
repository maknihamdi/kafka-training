package com.kafka.training.producer.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.MicrometerProducerListener;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaMetricsConfig {

    @Bean
    public MicrometerProducerListener<String, Object> micrometerProducerListener(
            MeterRegistry meterRegistry,
            ProducerFactory<String, Object> producerFactory) {

        MicrometerProducerListener<String, Object> listener = new MicrometerProducerListener<>(meterRegistry);

        // Add the listener to the producer factory
        producerFactory.addListener(listener);

        return listener;
    }
}
