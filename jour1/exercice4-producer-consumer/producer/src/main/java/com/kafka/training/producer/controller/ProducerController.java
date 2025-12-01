package com.kafka.training.producer.controller;

import com.kafka.training.producer.model.Message;
import com.kafka.training.producer.service.MessageProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
public class ProducerController {

    private static final Logger log = LoggerFactory.getLogger(ProducerController.class);
    private final MessageProducerService producerService;

    public ProducerController(MessageProducerService producerService) {
        this.producerService = producerService;
    }

    /**
     * Endpoint pour envoyer un message avec une clé
     * POST /api/messages/send
     *
     * Body:
     * {
     *   "topic": "messages",
     *   "key": "user-123",
     *   "content": "Hello Kafka!",
     *   "sender": "Alice"
     * }
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendMessage(@RequestBody Map<String, String> request) {
        String topic = request.getOrDefault("topic", "messages");
        String key = request.get("key");
        String content = request.get("content");
        String sender = request.getOrDefault("sender", "anonymous");

        Message message = new Message(
                UUID.randomUUID().toString(),
                content,
                sender,
                LocalDateTime.now()
        );

        if (key != null && !key.isEmpty()) {
            producerService.sendMessage(topic, key, message);
        } else {
            producerService.sendMessage(topic, message);
        }

        return ResponseEntity.ok(Map.of(
                "status", "sent",
                "topic", topic,
                "key", key != null ? key : "null",
                "messageId", message.getId()
        ));
    }

    /**
     * Endpoint pour envoyer plusieurs messages rapidement
     * POST /api/messages/send-batch
     *
     * Body:
     * {
     *   "topic": "messages",
     *   "count": 10,
     *   "sender": "Alice"
     * }
     */
    @PostMapping("/send-batch")
    public ResponseEntity<Map<String, Object>> sendBatch(@RequestBody Map<String, Object> request) {
        String topic = (String) request.getOrDefault("topic", "messages");
        int count = (int) request.getOrDefault("count", 10);
        String sender = (String) request.getOrDefault("sender", "batch-sender");

        log.info("Envoi de {} messages vers le topic '{}'", count, topic);

        for (int i = 0; i < count; i++) {
            Message message = new Message(
                    UUID.randomUUID().toString(),
                    "Batch message #" + (i + 1),
                    sender,
                    LocalDateTime.now()
            );

            // Utiliser un modulo pour distribuer sur quelques clés
            String key = "key-" + (i % 4);
            producerService.sendMessage(topic, key, message);
        }

        return ResponseEntity.ok(Map.of(
                "status", "sent",
                "topic", topic,
                "count", count
        ));
    }

    /**
     * Endpoint de health check
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "producer"));
    }
}
