package com.kafka.training.producer.service;

import com.kafka.training.common.model.Quote;
import com.kafka.training.common.model.ProductPricing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class QuoteProducerService {

    private static final Logger log = LoggerFactory.getLogger(QuoteProducerService.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public QuoteProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    private static final String QUOTE_EVENTS_TOPIC = "devis-events";
    private static final String PRODUCT_PRICING_TOPIC = "product-pricing";

    public void sendQuote(Quote quote) {
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(QUOTE_EVENTS_TOPIC, quote.getQuoteId(), quote);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Quote sent successfully: quoteId={}, status={}, customerId={}, partition={}",
                        quote.getQuoteId(), quote.getStatus(), quote.getCustomerId(),
                        result.getRecordMetadata().partition());
            } else {
                log.error("Failed to send quote: {}", ex.getMessage());
            }
        });
    }

    public void sendProductPricing(ProductPricing pricing) {
        // Utilise productCode comme clé pour garantir le compactage
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(PRODUCT_PRICING_TOPIC, pricing.getProductCode(), pricing);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Product pricing sent: productCode={}, basePrice={}, partition={}",
                        pricing.getProductCode(), pricing.getBasePrice(),
                        result.getRecordMetadata().partition());
            } else {
                log.error("Failed to send product pricing: {}", ex.getMessage());
            }
        });
    }
}
