package com.kafka.training.streams.config;

import com.kafka.training.common.model.EnrichedQuote;
import com.kafka.training.common.model.Quote;
import com.kafka.training.common.model.ProductPricing;
import com.kafka.training.common.model.QuoteAggregate;
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
    public JsonSerde<Quote> quoteSerde() {
        return new JsonSerde<>(Quote.class);
    }

    @Bean
    public JsonSerde<ProductPricing> productPricingSerde() {
        return new JsonSerde<>(ProductPricing.class);
    }

    @Bean
    public JsonSerde<EnrichedQuote> enrichedQuoteSerde() {
        return new JsonSerde<>(EnrichedQuote.class);
    }

    @Bean
    public JsonSerde<QuoteAggregate> quoteAggregateSerde() {
        return new JsonSerde<>(QuoteAggregate.class);
    }
}
