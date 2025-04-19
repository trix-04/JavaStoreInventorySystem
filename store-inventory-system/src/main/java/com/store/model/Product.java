package com.store.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

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

    // Default constructor needed for Jackson
    public Product() {
    }

    public Product(String name, double price, int quantity, double discount) {
        this.name = name;
        this.price = BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP);
        this.quantity = quantity;
        this.discount = BigDecimal.valueOf(discount).setScale(2, RoundingMode.HALF_UP);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP);
    }

    public void setPrice(BigDecimal price) {
        this.price = price.setScale(2, RoundingMode.HALF_UP);
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = BigDecimal.valueOf(discount).setScale(2, RoundingMode.HALF_UP);
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotalValue() {
        BigDecimal total = price.multiply(BigDecimal.valueOf(quantity));
        BigDecimal discountAmount = total.multiply(discount);
        return total.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return String.format("Product: %s, Price: $%.2f, Quantity: %d, Discount: %.0f%%",
                name, price, quantity, discount.multiply(BigDecimal.valueOf(100)));
    }
}
