package com.kafka.training.consumer.service;

import com.kafka.training.consumer.model.Message;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class MessageConsumerService {

    private static final Logger log = LoggerFactory.getLogger(MessageConsumerService.class);

    @Value("${kafka.consumer.topic:messages}")
    private String topic;

    @Value("${kafka.consumer.partitions:4}")
    private int partitionCount;

    private final KafkaConsumer<String, Message> consumer;
    private volatile boolean running = true;

    public MessageConsumerService(KafkaConsumer<String, Message> consumer) {
        this.consumer = consumer;
    }

    /**
     * Démarre la consommation au lancement de l'application
     * Consumer STANDALONE - utilise assign() au lieu de subscribe()
     * Pas besoin de group.id avec cette approche
     */
    @PostConstruct
    public void startConsuming() {
        // Créer un thread séparé pour la consommation
        Thread consumerThread = new Thread(() -> {
            try {
                // Construire dynamiquement la liste des partitions
                List<TopicPartition> partitions = new ArrayList<>();
                for (int i = 0; i < partitionCount; i++) {
                    partitions.add(new TopicPartition(topic, i));
                }

                log.info("📋 Configuration du consumer:");
                log.info("   Topic: {}", topic);
                log.info("   Nombre de partitions: {}", partitionCount);

                consumer.assign(partitions);

                // Commencer au début (équivalent de earliest)
                consumer.seekToBeginning(partitions);

                log.info("🚀 Consumer standalone démarré");
                log.info("📌 Partitions assignées: {}", partitions);

                // Boucle infinie de consommation
                while (running) {
                    ConsumerRecords<String, Message> records = consumer.poll(Duration.ofMillis(1000));

                    for (ConsumerRecord<String, Message> record : records) {
                        log.info("═══════════════════════════════════════════════════════");
                        log.info("📩 Message reçu du topic 'messages'");
                        log.info("   Partition: {}", record.partition());
                        log.info("   Offset: {}", record.offset());
                        log.info("   Key: {}", record.key());
                        log.info("   Message: {}", record.value());
                        log.info("═══════════════════════════════════════════════════════");

                        // Traitement du message
                        processMessage(record.value());
                    }
                }
            } catch (Exception e) {
                log.error("❌ Erreur dans le consumer", e);
            }
        });

        consumerThread.setName("kafka-consumer-thread");
        consumerThread.start();
    }

    /**
     * Traite le message reçu
     */
    private void processMessage(Message message) {
        // Simuler un traitement
        try {
            Thread.sleep(100);
            log.debug("✅ Message traité: {}", message.getId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("❌ Erreur lors du traitement du message", e);
        }
    }

    /**
     * Arrête proprement le consumer lors de l'arrêt de l'application
     */
    @PreDestroy
    public void stopConsuming() {
        log.info("🛑 Arrêt du consumer...");
        running = false;
        if (consumer != null) {
            consumer.wakeup();
            consumer.close();
        }
        log.info("✅ Consumer arrêté");
    }
}
