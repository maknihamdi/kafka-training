package com.kafka.training.common.model;


public class EnrichedEvent {
    private String userId;
    private String eventType;
    private Double amount;
    private String country;
    private Long timestamp;

    // Enriched fields from UserProfile
    private String userName;
    private String userTier;

    public EnrichedEvent() {
    }

    public EnrichedEvent(String userId, String eventType, Double amount, String country, Long timestamp, String userName, String userTier) {
        this.userId = userId;
        this.eventType = eventType;
        this.amount = amount;
        this.country = country;
        this.timestamp = timestamp;
        this.userName = userName;
        this.userTier = userTier;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserTier() {
        return userTier;
    }

    public void setUserTier(String userTier) {
        this.userTier = userTier;
    }
}
