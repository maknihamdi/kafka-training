package com.kafka.training.scalableconsumer.service;

import com.kafka.training.scalableconsumer.model.Message;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Service
public class MessageConsumerService {

    private static final Logger log = LoggerFactory.getLogger(MessageConsumerService.class);

    @Value("${spring.application.name:scalable-consumer}")
    private String applicationName;

    private final String instanceId;

    public MessageConsumerService() {
        // Générer un ID unique pour cette instance
        String tempInstanceId;
        try {
            tempInstanceId = InetAddress.getLocalHost().getHostName() + "-" + System.currentTimeMillis();
        } catch (UnknownHostException e) {
            tempInstanceId = "unknown-" + System.currentTimeMillis();
        }
        this.instanceId = tempInstanceId;
    }

    /**
     * Consumer AVEC consumer group
     * Utilise @KafkaListener avec un groupId
     *
     * Caractéristiques:
     * - Les partitions sont automatiquement réparties entre les instances du groupe
     * - Rebalancing automatique en cas d'ajout/suppression d'instances
     * - Chaque message est consommé par UNE SEULE instance du groupe
     * - Scalabilité horizontale: ajoutez plus d'instances pour augmenter le débit
     */
    @KafkaListener(
            topics = "${kafka.consumer.topic:messages}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeMessages(
            ConsumerRecord<String, Message> record,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.GROUP_ID) String groupId) {

        log.info("╔════════════════════════════════════════════════════════════════╗");
        log.info("║ 📩 Message reçu - Consumer Group Mode                         ║");
        log.info("╠════════════════════════════════════════════════════════════════╣");
        log.info("║ Instance ID  : {}", String.format("%-45s", instanceId));
        log.info("║ Group ID     : {}", String.format("%-45s", groupId));
        log.info("║ Topic        : {}", String.format("%-45s", topic));
        log.info("║ Partition    : {}", String.format("%-45s", partition));
        log.info("║ Offset       : {}", String.format("%-45s", record.offset()));
        log.info("║ Key          : {}", String.format("%-45s", record.key()));
        log.info("╠════════════════════════════════════════════════════════════════╣");
        log.info("║ Message      : {}", record.value());
        log.info("╚════════════════════════════════════════════════════════════════╝");

        // Traitement du message
        processMessage(record.value(), partition);
    }

    /**
     * Traite le message reçu
     */
    private void processMessage(Message message, int partition) {
        try {
            // Simuler un traitement
            Thread.sleep(100);
            log.debug("✅ [Instance: {}] Message traité: {} (Partition: {})",
                    instanceId, message.getId(), partition);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("❌ Erreur lors du traitement du message", e);
        }
    }
}
