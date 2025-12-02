package com.kafka.training.streams.topology;

import com.kafka.training.common.model.EnrichedEvent;
import com.kafka.training.common.model.Event;
import com.kafka.training.common.model.UserProfile;
import com.kafka.training.common.serde.JsonSerde;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Pipeline Kafka Streams simple pour débuter
 *
 * Cette topologie :
 * 1. Lit les événements depuis user-events
 * 2. Filtre uniquement les événements PURCHASE
 * 3. Écrit dans filtered-events
 *
 * EXERCICE : Faire évoluer cette pipeline pour explorer Kafka Streams
 * - Ajouter des transformations (map, mapValues)
 * - Créer des agrégations (count, sum)
 * - Faire des jointures avec user-profiles (KTable)
 * - Utiliser des fenêtres temporelles
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class EventStreamTopology {

    private final JsonSerde<Event> eventSerde;
    private final JsonSerde<UserProfile> userProfileSerde;
    private final JsonSerde<EnrichedEvent> enrichedEventSerde;

    @Autowired
    public void buildPipeline(StreamsBuilder streamsBuilder) {
        log.info("Building Kafka Streams topology...");

        // ========================================
        // PIPELINE SIMPLE : Filtre des PURCHASE
        // ========================================

        // 1. Stream des événements
        KStream<String, Event> eventsStream = streamsBuilder
                .stream("user-events", Consumed.with(Serdes.String(), eventSerde))
                .peek((key, value) -> log.debug("Received event: key={}, type={}", key, value.getEventType()));

        // 2. Filtre : uniquement les PURCHASE
        KStream<String, Event> purchaseStream = eventsStream
                .filter((key, event) -> "PURCHASE".equals(event.getEventType()))
                .peek((key, value) -> log.info("Filtered PURCHASE: userId={}, amount={}",
                        value.getUserId(), value.getAmount()));

        // 3. Écriture dans le topic de sortie
        purchaseStream.to("filtered-events", Produced.with(Serdes.String(), eventSerde));

        // ========================================
        // À FAIRE PAR LES ÉLÈVES
        // ========================================

        // TODO 1: Créer un KTable depuis user-profiles (topic compacté)
        // KTable<String, UserProfile> userProfileTable = streamsBuilder.table(...);

        // TODO 2: Faire une jointure entre purchaseStream et userProfileTable
        // KStream<String, EnrichedEvent> enrichedStream = purchaseStream.join(
        //     userProfileTable,
        //     (event, profile) -> new EnrichedEvent(...)
        // );

        // TODO 3: Créer des agrégations
        // - Compter les achats par utilisateur
        // - Calculer le montant total par pays
        // - Utiliser des fenêtres temporelles (ex: fenêtre de 1 minute)

        // TODO 4: Filtres avancés
        // - Filtrer les achats > 100€
        // - Filtrer par pays
        // - Filtrer par tier d'utilisateur (GOLD uniquement)

        log.info("Kafka Streams topology built successfully");
    }
}
