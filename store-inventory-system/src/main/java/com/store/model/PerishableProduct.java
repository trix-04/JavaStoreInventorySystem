package com.store.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.math.RoundingMode;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("perishable")
public class PerishableProduct extends Product {
    private LocalDate expirationDate;
    
    // Default constructor needed for Jackson
    public PerishableProduct() {
        super();
    }

    public PerishableProduct(String name, double price, int quantity, String expirationDate, double discount) {
        super(name, price, quantity, discount);
        this.expirationDate = LocalDate.parse(expirationDate, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = LocalDate.parse(expirationDate, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    @Override
    public BigDecimal getTotalValue() {
        BigDecimal baseValue = super.getTotalValue();
        // Apply additional 20% discount if product is not expired and expiring within 7 days
        LocalDate now = LocalDate.now();
        if (!expirationDate.isBefore(now) && expirationDate.isBefore(now.plusDays(7))) {
            return baseValue.multiply(BigDecimal.valueOf(0.8)).setScale(2, RoundingMode.HALF_UP);
        }
        return baseValue;
    }

    @Override
    public String toString() {
        return String.format("%s, Expiration Date: %s", 
                super.toString(), 
                expirationDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
    }
}
