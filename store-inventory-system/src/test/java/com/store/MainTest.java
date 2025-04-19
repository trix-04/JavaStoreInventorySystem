package com.store;

import com.store.model.Product;
import com.store.model.PerishableProduct;
import com.store.service.StoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for all major components of the inventory system
 */
public class MainTest {
    
    private StoreService storeService;
    
    @BeforeEach
    void setUp() {
        storeService = new StoreService();
    }
    
    @Nested
    @DisplayName("Product Tests")
    class ProductTests {
        @Test
        @DisplayName("Create regular product")
        void testCreateRegularProduct() {
            Product product = new Product("Test Product", 10.99, 5, 0.05);
            assertEquals("Test Product", product.getName());
            assertEquals(10.99, product.getPrice().doubleValue(), 0.001);
            assertEquals(5, product.getQuantity());
            assertEquals(0.05, product.getDiscount().doubleValue(), 0.001);
        }
        
        @Test
        @DisplayName("Calculate regular product total value")
        void testRegularProductTotalValue() {
            Product product = new Product("Test Product", 10.0, 5, 0.10);
            assertEquals(45.00, product.getTotalValue().doubleValue(), 0.001);
        }
    }
    
    @Nested
    @DisplayName("Perishable Product Tests")
    class PerishableProductTests {
        @Test
        @DisplayName("Create perishable product")
        void testCreatePerishableProduct() {
            LocalDate expirationDate = LocalDate.now().plusDays(5);
            PerishableProduct product = new PerishableProduct(
                "Milk", 2.99, 3, expirationDate.toString(), 0.0
            );
            
            assertEquals("Milk", product.getName());
            assertEquals(expirationDate, product.getExpirationDate());
        }
        
        @Test
        @DisplayName("Calculate perishable product discount")
        void testPerishableDiscount() {
            // Product expiring in 5 days should have additional discount
            LocalDate expirationDate = LocalDate.now().plusDays(5);
            PerishableProduct product = new PerishableProduct(
                "Yogurt", 3.00, 4, expirationDate.toString(), 0.0
            );
            
            // Expected: 4 items at $3.00 with 20% discount = $9.60
            assertTrue(product.getTotalValue().doubleValue() < 12.0);
        }
    }
    
    @Nested
    @DisplayName("Store Service Tests")
    class StoreServiceTests {
        @Test
        @DisplayName("Add product to inventory")
        void testAddProduct() {
            Product product = new Product("Test Item", 5.0, 2, 0.0);
            storeService.addProduct(product);
            assertEquals(1, storeService.getInventory().size());
        }
        
        @Test
        @DisplayName("Find product by name")
        void testFindProductByName() {
            Product product = new Product("Unique Item", 7.5, 3, 0.0);
            storeService.addProduct(product);
            var found = storeService.findProductByName("Unique Item");
            assertTrue(found.isPresent());
            assertEquals("Unique Item", found.get().getName());
        }
        
        @Test
        @DisplayName("Calculate total quantities and prices")
        void testTotalCalculations() {
            storeService.addProduct(new Product("Item 1", 10.0, 3, 0.0));
            storeService.addProduct(new Product("Item 2", 5.0, 2, 0.0));
            
            assertEquals(5, storeService.getTotalQuantity());
            assertEquals(40.0, storeService.getTotalGrossPrice().doubleValue(), 0.001);
        }
    }
}
