package com.kafka.training.common.model;

public class Event {
    private String userId;
    private String eventType;
    private Double amount;
    private String country;
    private Long timestamp;

    public Event() {
    }

    public Event(String userId, String eventType, Double amount, String country, Long timestamp) {
        this.userId = userId;
        this.eventType = eventType;
        this.amount = amount;
        this.country = country;
        this.timestamp = timestamp;
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
}
