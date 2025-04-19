package com.store.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Subclass representing a perishable product with expiration date.
 * This class extends the base Product class and adds expiration date functionality.
 * It provides special pricing rules based on expiration date proximity.
 */
@JsonTypeName("perishable")
public class PerishableProduct extends Product {
    private LocalDate expirationDate;
    
    /**
     * Default constructor needed for Jackson deserialization
     */
    public PerishableProduct() {
        super();
    }

    /**
     * Creates a new perishable product with specified attributes
     * 
     * @param name           The product name
     * @param price          The product price per unit
     * @param quantity       The quantity in stock
     * @param expirationDate The expiration date in ISO format (yyyy-MM-dd)
     * @param discount       The discount rate (0.0-1.0)
     */
    public PerishableProduct(String name, double price, int quantity, String expirationDate, double discount) {
        super(name, price, quantity, discount);
        this.expirationDate = LocalDate.parse(expirationDate, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * @return The expiration date
     */
    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    /**
     * @param expirationDate The expiration date in ISO format (yyyy-MM-dd)
     */
    public void setExpirationDate(String expirationDate) {
        this.expirationDate = LocalDate.parse(expirationDate, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * @param expirationDate The expiration date to set
     */
    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    /**
     * Calculates the total value of this product with additional discount for approaching expiration.
     * Discount rules:
     * - If expiring in more than 7 days: normal discount
     * - If expiring within 3-7 days: additional 30% discount
     * - If expiring within 1-2 days: additional 50% discount
     * 
     * @return The total value with all discounts applied
     */
    @Override
    public BigDecimal getTotalValue() {
        BigDecimal baseValue = super.getTotalValue();
        
        // Calculate days until expiration
        LocalDate today = LocalDate.now();
        long daysUntilExpiration = ChronoUnit.DAYS.between(today, expirationDate);
        
        // Apply additional discount based on expiration proximity
        if (daysUntilExpiration <= 2 && daysUntilExpiration >= 0) {
            // 50% additional discount if expiring within 2 days
            return baseValue.multiply(BigDecimal.valueOf(0.5)).setScale(2, RoundingMode.HALF_UP);
        } else if (daysUntilExpiration <= 7 && daysUntilExpiration > 2) {
            // 30% additional discount if expiring within 3-7 days
            return baseValue.multiply(BigDecimal.valueOf(0.7)).setScale(2, RoundingMode.HALF_UP);
        } else if (daysUntilExpiration < 0) {
            // Product has expired - heavily discounted (80% off)
            return baseValue.multiply(BigDecimal.valueOf(0.2)).setScale(2, RoundingMode.HALF_UP);
        }
        
        // No additional discount if expiration date is far away
        return baseValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("%s, Expires: %s", super.toString(), expirationDate);
    }
}
