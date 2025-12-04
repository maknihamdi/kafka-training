package com.kafka.training.producer.service;

import com.kafka.training.common.model.Event;
import com.kafka.training.common.model.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class EventProducerService {

    private static final Logger log = LoggerFactory.getLogger(EventProducerService.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public EventProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    private static final String USER_EVENTS_TOPIC = "user-events";
    private static final String USER_PROFILES_TOPIC = "user-profiles";

    public void sendEvent(Event event) {
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(USER_EVENTS_TOPIC, event.getUserId(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Event sent successfully: userId={}, eventType={}, partition={}",
                        event.getUserId(), event.getEventType(), result.getRecordMetadata().partition());
            } else {
                log.error("Failed to send event: {}", ex.getMessage());
            }
        });
    }

    public void sendUserProfile(UserProfile profile) {
        // Utilise userId comme clé pour garantir le compactage
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(USER_PROFILES_TOPIC, profile.getUserId(), profile);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("User profile sent: userId={}, tier={}, partition={}",
                        profile.getUserId(), profile.getTier(), result.getRecordMetadata().partition());
            } else {
                log.error("Failed to send user profile: {}", ex.getMessage());
            }
        });
    }
}
