package com.kafka.training.queryapi.controller;

import com.kafka.training.common.model.EnrichedQuote;
import com.kafka.training.queryapi.service.QuoteQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quotes")
public class QuoteQueryController {

    private final QuoteQueryService queryService;

    public QuoteQueryController(QuoteQueryService queryService) {
        this.queryService = queryService;
    }

    /**
     * Récupère un devis par son ID
     * GET /api/quotes/{quoteId}
     */
    @GetMapping("/{quoteId}")
    public ResponseEntity<EnrichedQuote> getQuoteById(@PathVariable String quoteId) {
        return queryService.getQuoteById(quoteId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Récupère tous les devis (limité à 100)
     * GET /api/quotes?limit=50
     */
    @GetMapping
    public ResponseEntity<List<EnrichedQuote>> getAllQuotes(
            @RequestParam(defaultValue = "100") int limit) {
        List<EnrichedQuote> quotes = queryService.getAllQuotes(limit);
        return ResponseEntity.ok(quotes);
    }

    /**
     * Récupère les devis d'un client
     * GET /api/quotes/customer/{customerId}
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<EnrichedQuote>> getQuotesByCustomer(@PathVariable String customerId) {
        List<EnrichedQuote> quotes = queryService.getQuotesByCustomer(customerId);
        return ResponseEntity.ok(quotes);
    }

    /**
     * Récupère les devis d'un produit
     * GET /api/quotes/product/{productCode}
     */
    @GetMapping("/product/{productCode}")
    public ResponseEntity<List<EnrichedQuote>> getQuotesByProduct(@PathVariable String productCode) {
        List<EnrichedQuote> quotes = queryService.getQuotesByProduct(productCode);
        return ResponseEntity.ok(quotes);
    }

    /**
     * Récupère des statistiques globales
     * GET /api/quotes/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = queryService.getStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Health check
     * GET /api/quotes/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "query-api"
        ));
    }
}
