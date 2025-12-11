package com.kafka.training.common.model;

public class QuoteAggregate {
    private String customerId;
    private Long windowStart;
    private Long windowEnd;
    private Long count;
    private Double totalPremium;

    public QuoteAggregate() {
    }

    public QuoteAggregate(String customerId, Long windowStart, Long windowEnd, Long count, Double totalPremium) {
        this.customerId = customerId;
        this.windowStart = windowStart;
        this.windowEnd = windowEnd;
        this.count = count;
        this.totalPremium = totalPremium;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public Long getWindowStart() {
        return windowStart;
    }

    public void setWindowStart(Long windowStart) {
        this.windowStart = windowStart;
    }

    public Long getWindowEnd() {
        return windowEnd;
    }

    public void setWindowEnd(Long windowEnd) {
        this.windowEnd = windowEnd;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Double getTotalPremium() {
        return totalPremium;
    }

    public void setTotalPremium(Double totalPremium) {
        this.totalPremium = totalPremium;
    }
}
