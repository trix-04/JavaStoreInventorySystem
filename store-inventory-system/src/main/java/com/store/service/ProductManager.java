package com.store.service;

import com.store.model.Product;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

/**
 * Interface defining the core operations for managing product inventory
 */
public interface ProductManager {
    /**
     * Add a product to the inventory
     * @param product The product to add
     */
    void addProduct(Product product);
    
    /**
     * Remove a product from the inventory by index
     * @param index The index of the product to remove
     * @return true if removal was successful, false otherwise
     */
    boolean removeProduct(int index);
    
    /**
     * Retrieve the complete inventory
     * @return List of all products
     */
    List<Product> getInventory();
    
    /**
     * Find a product by name
     * @param name The name to search for
     * @return An Optional containing the product if found, empty otherwise
     */
    Optional<Product> findProductByName(String name);
    
    /**
     * Get total quantity of all products
     * @return The sum of all product quantities
     */
    int getTotalQuantity();
    
    /**
     * Get the gross price of all inventory
     * @return The sum of price × quantity for all products
     */
    BigDecimal getTotalGrossPrice();
    
    /**
     * Calculate total price accounting for perishable discounts
     * @return The price with perishable discounts applied
     */
    BigDecimal getTotalPriceWithPerishableDiscount();
    
    /**
     * Calculate final price with additional store-wide discount
     * @return The final discounted price
     */
    BigDecimal getTotalNetPriceWithDiscount();
}
