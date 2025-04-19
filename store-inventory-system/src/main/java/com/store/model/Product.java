package com.store.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Base class representing a product in the inventory system.
 * This class provides core functionality for all product types.
 * It uses Jackson annotations for proper JSON serialization and deserialization
 * to support polymorphic types.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = PerishableProduct.class, name = "perishable")
})
@JsonTypeName("product")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Product {
    private String name;
    private BigDecimal price;
    private int quantity;
    private BigDecimal discount;

    /**
     * Default constructor needed for Jackson deserialization
     */
    public Product() {
    }

    /**
     * Creates a new product with specified attributes
     * 
     * @param name      The product name
     * @param price     The product price per unit
     * @param quantity  The quantity in stock
     * @param discount  The discount rate (0.0-1.0)
     */
    public Product(String name, double price, int quantity, double discount) {
        this.name = name;
        this.price = BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP);
        this.quantity = quantity;
        this.discount = BigDecimal.valueOf(discount).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * @return The product name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The product name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return The product price per unit
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * @param price The product price per unit to set
     */
    public void setPrice(double price) {
        this.price = BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * @param price The product price per unit to set
     */
    public void setPrice(BigDecimal price) {
        this.price = price.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * @return The quantity in stock
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * @param quantity The quantity to set
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * @return The discount rate (0.0-1.0)
     */
    public BigDecimal getDiscount() {
        return discount;
    }

    /**
     * @param discount The discount rate to set (0.0-1.0)
     */
    public void setDiscount(double discount) {
        this.discount = BigDecimal.valueOf(discount).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * @param discount The discount rate to set (0.0-1.0) 
     */
    public void setDiscount(BigDecimal discount) {
        this.discount = discount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates the total value of this product (price * quantity - discount).
     * 
     * @return The total value with discount applied
     */
    public BigDecimal getTotalValue() {
        BigDecimal total = price.multiply(BigDecimal.valueOf(quantity));
        BigDecimal discountAmount = total.multiply(discount);
        return total.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("Product: %s, Price: $%.2f, Quantity: %d, Discount: %.0f%%",
                name, price, quantity, discount.multiply(BigDecimal.valueOf(100)));
    }
}
