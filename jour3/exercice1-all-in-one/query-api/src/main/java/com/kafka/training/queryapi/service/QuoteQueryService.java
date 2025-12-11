package com.kafka.training.queryapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafka.training.common.model.EnrichedQuote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuoteQueryService {

    private static final Logger log = LoggerFactory.getLogger(QuoteQueryService.class);
    private final RedisTemplate<String, String> stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public QuoteQueryService(RedisTemplate<String, String> stringRedisTemplate,
                             ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Récupère un devis par son ID
     */
    public Optional<EnrichedQuote> getQuoteById(String quoteId) {
        try {
            String json = stringRedisTemplate.opsForValue().get(quoteId);
            if (json != null) {
                EnrichedQuote quote = objectMapper.readValue(json, EnrichedQuote.class);
                log.info("Quote found: {}", quoteId);
                return Optional.of(quote);
            }
            log.warn("Quote not found: {}", quoteId);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error reading quote from Redis: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Récupère tous les devis (limité aux 100 premiers)
     */
    public List<EnrichedQuote> getAllQuotes(int limit) {
        try {
            Set<String> keys = stringRedisTemplate.keys("Q-*");
            if (keys == null || keys.isEmpty()) {
                return Collections.emptyList();
            }

            return keys.stream()
                    .limit(limit)
                    .map(key -> {
                        try {
                            String json = stringRedisTemplate.opsForValue().get(key);
                            return json != null ? objectMapper.readValue(json, EnrichedQuote.class) : null;
                        } catch (Exception e) {
                            log.error("Error parsing quote {}: {}", key, e.getMessage());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error reading all quotes from Redis: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Récupère les devis d'un client
     */
    public List<EnrichedQuote> getQuotesByCustomer(String customerId) {
        try {
            Set<String> keys = stringRedisTemplate.keys("Q-*");
            if (keys == null || keys.isEmpty()) {
                return Collections.emptyList();
            }

            return keys.stream()
                    .map(key -> {
                        try {
                            String json = stringRedisTemplate.opsForValue().get(key);
                            return json != null ? objectMapper.readValue(json, EnrichedQuote.class) : null;
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .filter(quote -> customerId.equals(quote.getCustomerId()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error reading quotes by customer from Redis: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Récupère les devis d'un produit
     */
    public List<EnrichedQuote> getQuotesByProduct(String productCode) {
        try {
            Set<String> keys = stringRedisTemplate.keys("Q-*");
            if (keys == null || keys.isEmpty()) {
                return Collections.emptyList();
            }

            return keys.stream()
                    .map(key -> {
                        try {
                            String json = stringRedisTemplate.opsForValue().get(key);
                            return json != null ? objectMapper.readValue(json, EnrichedQuote.class) : null;
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .filter(quote -> productCode.equals(quote.getProductCode()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error reading quotes by product from Redis: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Calcule des statistiques globales
     */
    public Map<String, Object> getStatistics() {
        try {
            Set<String> keys = stringRedisTemplate.keys("Q-*");
            if (keys == null || keys.isEmpty()) {
                return Collections.emptyMap();
            }

            List<EnrichedQuote> allQuotes = keys.stream()
                    .map(key -> {
                        try {
                            String json = stringRedisTemplate.opsForValue().get(key);
                            return json != null ? objectMapper.readValue(json, EnrichedQuote.class) : null;
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalQuotes", allQuotes.size());
            stats.put("totalPremium", allQuotes.stream()
                    .mapToDouble(q -> q.getFinalPremium() != null ? q.getFinalPremium() : 0.0)
                    .sum());
            stats.put("averagePremium", allQuotes.stream()
                    .mapToDouble(q -> q.getFinalPremium() != null ? q.getFinalPremium() : 0.0)
                    .average()
                    .orElse(0.0));
            stats.put("byProduct", allQuotes.stream()
                    .collect(Collectors.groupingBy(
                            EnrichedQuote::getProductCode,
                            Collectors.counting()
                    )));

            return stats;
        } catch (Exception e) {
            log.error("Error computing statistics: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }
}
