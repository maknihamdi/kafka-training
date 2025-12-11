package com.kafka.training.common.model;

public class Quote {
    private String quoteId;
    private String customerId;
    private QuoteStatus status;
    private String productCode;
    private Double basePremium;
    private Double finalPremium;
    private Long createdAt;
    private Long updatedAt;

    public Quote() {
    }

    public Quote(String quoteId, String customerId, QuoteStatus status, String productCode,
                 Double basePremium, Double finalPremium, Long createdAt, Long updatedAt) {
        this.quoteId = quoteId;
        this.customerId = customerId;
        this.status = status;
        this.productCode = productCode;
        this.basePremium = basePremium;
        this.finalPremium = finalPremium;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(String quoteId) {
        this.quoteId = quoteId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public QuoteStatus getStatus() {
        return status;
    }

    public void setStatus(QuoteStatus status) {
        this.status = status;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public Double getBasePremium() {
        return basePremium;
    }

    public void setBasePremium(Double basePremium) {
        this.basePremium = basePremium;
    }

    public Double getFinalPremium() {
        return finalPremium;
    }

    public void setFinalPremium(Double finalPremium) {
        this.finalPremium = finalPremium;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
