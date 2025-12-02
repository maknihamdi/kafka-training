package com.kafka.training.producer.controller;

import com.kafka.training.common.model.Event;
import com.kafka.training.common.model.UserProfile;
import com.kafka.training.producer.service.EventProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProducerController {

    private final EventProducerService producerService;
    private final Random random = new Random();

    private static final List<String> USER_IDS = Arrays.asList("user1", "user2", "user3", "user4", "user5");
    private static final List<String> EVENT_TYPES = Arrays.asList("PURCHASE", "LOGIN", "LOGOUT", "VIEW", "ADD_TO_CART");
    private static final List<String> COUNTRIES = Arrays.asList("France", "USA", "Germany", "UK", "Spain");

    @PostMapping("/events")
    public ResponseEntity<String> sendEvent(@RequestBody Event event) {
        if (event.getTimestamp() == null) {
            event.setTimestamp(System.currentTimeMillis());
        }
        producerService.sendEvent(event);
        return ResponseEntity.ok("Event sent");
    }

    @PostMapping("/events/generate")
    public ResponseEntity<String> generateEvents(@RequestParam(defaultValue = "10") int count) {
        for (int i = 0; i < count; i++) {
            Event event = new Event(
                    USER_IDS.get(random.nextInt(USER_IDS.size())),
                    EVENT_TYPES.get(random.nextInt(EVENT_TYPES.size())),
                    random.nextDouble() * 200,
                    COUNTRIES.get(random.nextInt(COUNTRIES.size())),
                    System.currentTimeMillis()
            );
            producerService.sendEvent(event);

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return ResponseEntity.ok(count + " events generated");
    }

    @PostMapping("/profiles")
    public ResponseEntity<String> sendUserProfile(@RequestBody UserProfile profile) {
        producerService.sendUserProfile(profile);
        return ResponseEntity.ok("User profile sent");
    }

    @PostMapping("/profiles/init")
    public ResponseEntity<String> initUserProfiles() {
        List<UserProfile> profiles = Arrays.asList(
                new UserProfile("user1", "Alice", "France", "GOLD"),
                new UserProfile("user2", "Bob", "USA", "SILVER"),
                new UserProfile("user3", "Charlie", "Germany", "BRONZE"),
                new UserProfile("user4", "Diana", "UK", "GOLD"),
                new UserProfile("user5", "Eve", "Spain", "SILVER")
        );

        profiles.forEach(producerService::sendUserProfile);
        return ResponseEntity.ok("User profiles initialized");
    }
}
