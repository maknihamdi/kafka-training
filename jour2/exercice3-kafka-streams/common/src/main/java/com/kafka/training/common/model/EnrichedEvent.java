package com.kafka.training.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrichedEvent {
    private String userId;
    private String eventType;
    private Double amount;
    private String country;
    private Long timestamp;

    // Enriched fields from UserProfile
    private String userName;
    private String userTier;
}
