package com.kafka.training.queryapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafka.training.common.model.EnrichedQuote;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, EnrichedQuote> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, EnrichedQuote> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key serializer
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Value serializer
        Jackson2JsonRedisSerializer<EnrichedQuote> serializer =
            new Jackson2JsonRedisSerializer<>(EnrichedQuote.class);

        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
