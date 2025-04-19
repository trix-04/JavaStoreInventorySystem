package com.store.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import com.store.model.Product;
import com.store.model.PerishableProduct;

import java.time.LocalDate;

class StoreServiceTest {

    private StoreService service;

    @BeforeEach
    void setUp() {
        service = new StoreService();
        // Clear inventory
        while (!service.getInventory().isEmpty()) {
            service.removeProduct(0);
        }
    }

    @Test
    void addProductTest() {
        Product p = new Product("Test Product", 10.0, 5, 0.1);
        service.addProduct(p);
        
        assertEquals(1, service.getInventory().size());
        assertEquals("Test Product", service.getInventory().get(0).getName());
    }

    @Test
    void totalQuantityTest() {
        service.addProduct(new Product("Product1", 10.0, 5, 0.0));
        service.addProduct(new Product("Product2", 20.0, 3, 0.0));
        
        assertEquals(8, service.getTotalQuantity());
    }

    @Test
    void totalGrossPriceTest() {
        service.addProduct(new Product("Product1", 10.0, 2, 0.0));
        service.addProduct(new Product("Product2", 20.0, 3, 0.0));
        
        // (10.0 * 2) + (20.0 * 3) = 20 + 60 = 80.0
        assertEquals(new BigDecimal("80.00").setScale(2, RoundingMode.HALF_UP), 
                    service.getTotalGrossPrice());
    }

    @Test
    void perishableProductDiscountTest() {
        // Get tomorrow's date for testing
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        
        // Create a perishable product expiring tomorrow (within 7 days)
        PerishableProduct p = new PerishableProduct(
            "Perishable", 10.0, 2, tomorrow.toString(), 0.0
        );
        
        // Should apply additional 20% discount
        BigDecimal expected = new BigDecimal("16.00").setScale(2, RoundingMode.HALF_UP);
        assertEquals(expected, p.getTotalValue());
    }
}
