package com.kafka.training.producer.controller;

import com.kafka.training.common.model.Quote;
import com.kafka.training.common.model.QuoteStatus;
import com.kafka.training.common.model.ProductPricing;
import com.kafka.training.producer.service.QuoteProducerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class QuoteController {

    private final QuoteProducerService producerService;
    private final Random random = new Random();

    public QuoteController(QuoteProducerService producerService) {
        this.producerService = producerService;
    }

    private static final List<String> CUSTOMER_IDS = Arrays.asList("C001", "C002", "C003", "C004", "C005");
    private static final List<String> PRODUCT_CODES = Arrays.asList("AUTO", "HOME", "HEALTH", "LIFE", "TRAVEL");

    @PostMapping("/quotes")
    public ResponseEntity<String> createQuote(@RequestBody Quote quote) {
        if (quote.getQuoteId() == null) {
            quote.setQuoteId("Q-" + UUID.randomUUID().toString().substring(0, 8));
        }
        if (quote.getCreatedAt() == null) {
            quote.setCreatedAt(System.currentTimeMillis());
        }
        if (quote.getUpdatedAt() == null) {
            quote.setUpdatedAt(System.currentTimeMillis());
        }
        if (quote.getStatus() == null) {
            quote.setStatus(QuoteStatus.DRAFT);
        }
        producerService.sendQuote(quote);
        return ResponseEntity.ok("Quote created: " + quote.getQuoteId());
    }

    @PostMapping("/quotes/{quoteId}/validate")
    public ResponseEntity<String> validateQuote(@PathVariable String quoteId) {
        long now = System.currentTimeMillis();
        Quote quote = new Quote(
                quoteId,
                CUSTOMER_IDS.get(random.nextInt(CUSTOMER_IDS.size())),
                QuoteStatus.VALIDATED,
                PRODUCT_CODES.get(random.nextInt(PRODUCT_CODES.size())),
                random.nextDouble() * 500 + 100,
                null,
                now - 3600000,
                now
        );
        producerService.sendQuote(quote);
        return ResponseEntity.ok("Quote validated: " + quoteId);
    }

    @PostMapping("/quotes/{quoteId}/cancel")
    public ResponseEntity<String> cancelQuote(@PathVariable String quoteId) {
        long now = System.currentTimeMillis();
        Quote quote = new Quote(
                quoteId,
                CUSTOMER_IDS.get(random.nextInt(CUSTOMER_IDS.size())),
                QuoteStatus.CANCELLED,
                PRODUCT_CODES.get(random.nextInt(PRODUCT_CODES.size())),
                random.nextDouble() * 500 + 100,
                null,
                now - 3600000,
                now
        );
        producerService.sendQuote(quote);
        return ResponseEntity.ok("Quote cancelled: " + quoteId);
    }

    @PostMapping("/quotes/generate")
    public ResponseEntity<String> generateQuotes(@RequestParam(defaultValue = "10") int count) {
        int validated = 0, cancelled = 0, draft = 0;

        for (int i = 0; i < count; i++) {
            QuoteStatus status;
            int statusChoice = random.nextInt(100);
            if (statusChoice < 60) {
                status = QuoteStatus.VALIDATED;
                validated++;
            } else if (statusChoice < 80) {
                status = QuoteStatus.DRAFT;
                draft++;
            } else {
                status = QuoteStatus.CANCELLED;
                cancelled++;
            }

            long now = System.currentTimeMillis();
            Quote quote = new Quote(
                    "Q-" + UUID.randomUUID().toString().substring(0, 8),
                    CUSTOMER_IDS.get(random.nextInt(CUSTOMER_IDS.size())),
                    status,
                    PRODUCT_CODES.get(random.nextInt(PRODUCT_CODES.size())),
                    random.nextDouble() * 500 + 100,
                    null,
                    now - random.nextInt(86400000),
                    now
            );
            producerService.sendQuote(quote);

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return ResponseEntity.ok(String.format("%d quotes generated (Validated: %d, Draft: %d, Cancelled: %d)",
                count, validated, draft, cancelled));
    }

    @PostMapping("/pricing")
    public ResponseEntity<String> sendProductPricing(@RequestBody ProductPricing pricing) {
        producerService.sendProductPricing(pricing);
        return ResponseEntity.ok("Product pricing sent");
    }

    @PostMapping("/pricing/init")
    public ResponseEntity<String> initProductPricing() {
        List<ProductPricing> pricingList = Arrays.asList(
                new ProductPricing("AUTO", "Auto Insurance", 500.0, 0.20),
                new ProductPricing("HOME", "Home Insurance", 800.0, 0.15),
                new ProductPricing("HEALTH", "Health Insurance", 1200.0, 0.10),
                new ProductPricing("LIFE", "Life Insurance", 2000.0, 0.05),
                new ProductPricing("TRAVEL", "Travel Insurance", 150.0, 0.25)
        );

        pricingList.forEach(pricing -> {
            producerService.sendProductPricing(pricing);
            System.out.println("Product pricing sent: " + pricing.getProductCode() +
                    " -> Base: " + pricing.getBasePrice() + ", Tax: " + pricing.getTaxRate());
        });

        return ResponseEntity.ok("Product pricing initialized");
    }
}
