package com.kafka.training.common.model;

public class UserProfile {
    private String userId;
    private String name;
    private String country;
    private String tier; // BRONZE, SILVER, GOLD

    public UserProfile() {
    }

    public UserProfile(String userId, String name, String country, String tier) {
        this.userId = userId;
        this.name = name;
        this.country = country;
        this.tier = tier;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }
}
