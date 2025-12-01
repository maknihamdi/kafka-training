package com.kafka.training.producer.service;

import com.kafka.training.producer.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class MessageProducerService {

    private static final Logger log = LoggerFactory.getLogger(MessageProducerService.class);
    private final KafkaTemplate<String, Message> kafkaTemplate;

    public MessageProducerService(KafkaTemplate<String, Message> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Envoie un message vers un topic Kafka
     *
     * @param topic   Le nom du topic
     * @param key     La clé du message (détermine la partition)
     * @param message Le message à envoyer
     */
    public void sendMessage(String topic, String key, Message message) {
        log.info("Envoi du message vers le topic '{}' avec la clé '{}': {}", topic, key, message);

        CompletableFuture<SendResult<String, Message>> future = kafkaTemplate.send(topic, key, message);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Message envoyé avec succès! Topic: {}, Partition: {}, Offset: {}, Key: {}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        key);
            } else {
                log.error("Erreur lors de l'envoi du message vers '{}' avec la clé '{}'", topic, key, ex);
            }
        });
    }

    /**
     * Envoie un message sans clé (distribution round-robin)
     *
     * @param topic   Le nom du topic
     * @param message Le message à envoyer
     */
    public void sendMessage(String topic, Message message) {
        log.info("Envoi du message vers le topic '{}' sans clé: {}", topic, message);

        CompletableFuture<SendResult<String, Message>> future = kafkaTemplate.send(topic, message);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Message envoyé avec succès! Topic: {}, Partition: {}, Offset: {}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Erreur lors de l'envoi du message vers '{}'", topic, ex);
            }
        });
    }
}
