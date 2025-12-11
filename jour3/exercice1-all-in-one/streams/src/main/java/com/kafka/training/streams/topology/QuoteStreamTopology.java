package com.kafka.training.streams.topology;

import com.kafka.training.common.model.EnrichedQuote;
import com.kafka.training.common.model.Quote;
import com.kafka.training.common.model.QuoteStatus;
import com.kafka.training.common.model.ProductPricing;
import com.kafka.training.common.model.QuoteAggregate;
import com.kafka.training.common.serde.JsonSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Pipeline Kafka Streams pour le traitement des devis d'assurance
 *
 * Cette topologie :
 * 1. Lit les devis depuis devis-events
 * 2. Filtre uniquement les devis validés
 * 3. Enrichit avec les prix des produits (KTable)
 * 4. Calcule la prime finale avec taxes
 * 5. Écrit dans validated-quotes et all-quotes
 */
@Component
public class QuoteStreamTopology {

    private static final Logger log = LoggerFactory.getLogger(QuoteStreamTopology.class);

    private final JsonSerde<Quote> quoteSerde;
    private final JsonSerde<ProductPricing> productPricingSerde;
    private final JsonSerde<EnrichedQuote> enrichedQuoteSerde;
    private final JsonSerde<QuoteAggregate> quoteAggregateSerde;

    public QuoteStreamTopology(JsonSerde<Quote> quoteSerde,
                               JsonSerde<ProductPricing> productPricingSerde,
                               JsonSerde<EnrichedQuote> enrichedQuoteSerde,
                               JsonSerde<QuoteAggregate> quoteAggregateSerde) {
        this.quoteSerde = quoteSerde;
        this.productPricingSerde = productPricingSerde;
        this.enrichedQuoteSerde = enrichedQuoteSerde;
        this.quoteAggregateSerde = quoteAggregateSerde;
    }

    @Autowired
    public void buildPipeline(StreamsBuilder streamsBuilder) {
        log.info("Building Kafka Streams topology for insurance quotes...");

        // ========================================
        // 1. STREAM DES DEVIS
        // ========================================
        KStream<String, Quote> quotesStream = streamsBuilder
                .stream("devis-events", Consumed.with(Serdes.String(), quoteSerde))
                .peek((key, value) -> log.debug("Received quote: quoteId={}, status={}",
                        key, value.getStatus()));

        // ========================================
        // 2. KTABLE DES PRIX PRODUITS (Compacté)
        // ========================================
        KTable<String, ProductPricing> productPricingTable = streamsBuilder
                .table("product-pricing",
                        Consumed.with(Serdes.String(), productPricingSerde),
                        Materialized.as("product-pricing-store"));

        // ========================================
        // 3. FILTRE : DEVIS VALIDÉS UNIQUEMENT
        // ========================================
        KStream<String, Quote> validatedQuotesStream = quotesStream
                .filter((key, quote) -> QuoteStatus.VALIDATED.equals(quote.getStatus()))
                .peek((key, quote) -> log.info("Validated quote: quoteId={}, customerId={}, productCode={}",
                        quote.getQuoteId(), quote.getCustomerId(), quote.getProductCode()));

        // Écrire les devis validés dans un topic dédié
        validatedQuotesStream.to("validated-quotes", Produced.with(Serdes.String(), quoteSerde));

        // ========================================
        // 4. JOINTURE : Enrichir avec les prix
        // ========================================
        // Re-keyer par productCode pour la jointure
        KStream<String, Quote> quotesKeyedByProduct = validatedQuotesStream
                .selectKey((key, quote) -> quote.getProductCode());

        KStream<String, EnrichedQuote> enrichedQuotesStream = quotesKeyedByProduct.join(
                productPricingTable,
                (quote, pricing) -> {
                    EnrichedQuote enriched = new EnrichedQuote();
                    enriched.setQuoteId(quote.getQuoteId());
                    enriched.setCustomerId(quote.getCustomerId());
                    enriched.setStatus(quote.getStatus());
                    enriched.setProductCode(quote.getProductCode());
                    enriched.setBasePremium(quote.getBasePremium());
                    enriched.setCreatedAt(quote.getCreatedAt());
                    enriched.setUpdatedAt(quote.getUpdatedAt());

                    // Enrichissement depuis ProductPricing
                    enriched.setProductName(pricing.getProductName());
                    enriched.setBasePrice(pricing.getBasePrice());
                    enriched.setTaxRate(pricing.getTaxRate());

                    // Calcul de la prime finale avec taxes
                    double finalPremium = quote.getBasePremium() * (1 + pricing.getTaxRate());
                    enriched.setFinalPremium(finalPremium);

                    log.info("Enriched quote: quoteId={}, productName={}, basePremium={}, finalPremium={}",
                            enriched.getQuoteId(), enriched.getProductName(),
                            enriched.getBasePremium(), enriched.getFinalPremium());

                    return enriched;
                },
                Joined.with(Serdes.String(), quoteSerde, productPricingSerde)
        );

        // Re-keyer par quoteId après jointure
        KStream<String, EnrichedQuote> enrichedQuotesKeyedById = enrichedQuotesStream
                .selectKey((key, quote) -> quote.getQuoteId());

        // ========================================
        // 5. ÉCRITURE DANS ALL-QUOTES (pour Redis)
        // ========================================
        enrichedQuotesKeyedById.to("all-quotes", Produced.with(Serdes.String(), enrichedQuoteSerde));

        // ========================================
        // 6. AGRÉGATIONS PAR CLIENT
        // ========================================
        // Fenêtre tumbling de 1 heure pour stats par client
        KTable<Windowed<String>, QuoteAggregate> quoteAggregateTable = validatedQuotesStream
                .selectKey((key, quote) -> quote.getCustomerId())
                .groupByKey(Grouped.with(Serdes.String(), quoteSerde))
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofHours(1)))
                .aggregate(
                        () -> new QuoteAggregate(null, null, null, 0L, 0.0),
                        (customerId, quote, aggregate) -> {
                            aggregate.setCustomerId(customerId);
                            aggregate.setCount(aggregate.getCount() + 1);
                            aggregate.setTotalPremium(aggregate.getTotalPremium() +
                                    (quote.getBasePremium() != null ? quote.getBasePremium() : 0.0));
                            return aggregate;
                        },
                        Materialized.with(Serdes.String(), quoteAggregateSerde)
                );

        // Logger les agrégations
        quoteAggregateTable.toStream()
                .map((windowed, aggregate) -> {
                    aggregate.setWindowStart(windowed.window().startTime().toEpochMilli());
                    aggregate.setWindowEnd(windowed.window().endTime().toEpochMilli());
                    log.info("Quote aggregate: customerId={}, count={}, totalPremium={}, window=[{}-{}]",
                            aggregate.getCustomerId(), aggregate.getCount(),
                            aggregate.getTotalPremium(), aggregate.getWindowStart(),
                            aggregate.getWindowEnd());
                    return new KeyValue<>(windowed.key(), aggregate);
                })
                .to("quote-aggregates", Produced.with(Serdes.String(), quoteAggregateSerde));

        log.info("Kafka Streams topology for insurance quotes built successfully");
    }
}
