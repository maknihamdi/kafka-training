package com.kafka.training.common.model;

public class ProductPricing {
    private String productCode;
    private String productName;
    private Double basePrice;
    private Double taxRate;

    public ProductPricing() {
    }

    public ProductPricing(String productCode, String productName, Double basePrice, Double taxRate) {
        this.productCode = productCode;
        this.productName = productName;
        this.basePrice = basePrice;
        this.taxRate = taxRate;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(Double basePrice) {
        this.basePrice = basePrice;
    }

    public Double getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(Double taxRate) {
        this.taxRate = taxRate;
    }
}
