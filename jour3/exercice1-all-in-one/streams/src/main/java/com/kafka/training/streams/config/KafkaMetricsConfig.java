package com.kafka.training.streams.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.kafka.KafkaStreamsMetrics;
import org.apache.kafka.streams.KafkaStreams;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;

@Configuration
public class KafkaMetricsConfig {

    private final MeterRegistry meterRegistry;
    private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    public KafkaMetricsConfig(MeterRegistry meterRegistry,
                              StreamsBuilderFactoryBean streamsBuilderFactoryBean) {
        this.meterRegistry = meterRegistry;
        this.streamsBuilderFactoryBean = streamsBuilderFactoryBean;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void registerKafkaStreamsMetrics() {
        KafkaStreams kafkaStreams = streamsBuilderFactoryBean.getKafkaStreams();
        if (kafkaStreams != null) {
            new KafkaStreamsMetrics(kafkaStreams).bindTo(meterRegistry);
        }
    }
}
