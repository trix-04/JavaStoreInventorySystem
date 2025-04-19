package com.store;

import com.store.model.Product;
import com.store.model.PerishableProduct;
import com.store.service.StoreService;
import com.store.service.ProductManager;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Comprehensive test class for store inventory system.
 * Tests all major components including models, service layer, and core functionality.
 */
@DisplayName("Store Inventory System Tests")
public class MainTest {
    
    private ProductManager productManager;
    
    @BeforeEach
    void setUp() {
        // Use interface type to demonstrate proper OOP principles
        productManager = StoreService.getInstance();
        
        // Clear existing data before each test
        List<Product> products = productManager.getInventory();
        while (!products.isEmpty()) {
            productManager.removeProduct(0);
            products = productManager.getInventory();
        }
    }
    
    @Nested
    @DisplayName("Product Class Tests")
    class ProductTests {
        @Test
        @DisplayName("Create regular product")
        void testCreateRegularProduct() {
            Product product = new Product("Test Product", 10.99, 5, 0.05);
            assertEquals("Test Product", product.getName(), "Product name should match");
            assertEquals(10.99, product.getPrice().doubleValue(), 0.001, "Price should match");
            assertEquals(5, product.getQuantity(), "Quantity should match");
            assertEquals(0.05, product.getDiscount().doubleValue(), 0.001, "Discount should match");
        }
        
        @Test
        @DisplayName("Calculate total value with discount")
        void testRegularProductTotalValue() {
            // Create product with 10% discount
            Product product = new Product("Discounted Item", 10.0, 5, 0.10);
            
            // Expected: 5 items at $10 each with 10% discount = $45.00
            assertEquals(45.00, product.getTotalValue().doubleValue(), 0.001, 
                    "Total value calculation should apply discount correctly");
        }
        
        @Test
        @DisplayName("Test string representation")
        void testToString() {
            Product product = new Product("Widget", 19.99, 3, 0.0);
            String productString = product.toString();
            
            assertTrue(productString.contains("Widget"), "Product string should contain the name");
            assertTrue(productString.contains("19.99"), "Product string should contain the price");
            assertTrue(productString.contains("3"), "Product string should contain the quantity");
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
            
            assertEquals("Milk", product.getName(), "Product name should match");
            assertEquals(expirationDate, product.getExpirationDate(), "Expiration date should match");
            assertEquals(2.99, product.getPrice().doubleValue(), 0.001, "Price should match");
        }
        
        @Test
        @DisplayName("Calculate perishable product discount based on expiration")
        void testPerishableDiscount() {
            // Product expiring in 2 days should have additional discount
            LocalDate expirationDate = LocalDate.now().plusDays(2);
            PerishableProduct product = new PerishableProduct(
                "Yogurt", 3.00, 4, expirationDate.toString(), 0.0
            );
            
            // Expected value should be less than full price due to expiration discount
            double fullPrice = 3.00 * 4; // $12.00
            assertTrue(product.getTotalValue().doubleValue() < fullPrice, 
                    "Perishable products should have additional discount when close to expiration");
        }
        
        @Test
        @DisplayName("Test expiration date handling")
        void testExpirationDate() {
            // Test setting expiration date via string
            PerishableProduct product1 = new PerishableProduct();
            product1.setExpirationDate("2023-12-31");
            assertEquals(LocalDate.of(2023, 12, 31), product1.getExpirationDate());
            
            // Test setting expiration date via LocalDate
            LocalDate date = LocalDate.of(2024, 1, 15);
            PerishableProduct product2 = new PerishableProduct();
            product2.setExpirationDate(date);
            assertEquals(date, product2.getExpirationDate());
        }
    }
    
    @Nested
    @DisplayName("Store Service Tests")
    class StoreServiceTests {
        @Test
        @DisplayName("Add product to inventory")
        void testAddProduct() {
            Product product = new Product("Test Item", 5.0, 2, 0.0);
            productManager.addProduct(product);
            
            List<Product> inventory = productManager.getInventory();
            assertEquals(1, inventory.size(), "Inventory should contain one product");
            assertEquals("Test Item", inventory.get(0).getName(), "Product name should match");
        }
        
        @Test
        @DisplayName("Remove product from inventory")
        void testRemoveProduct() {
            // Add two products
            productManager.addProduct(new Product("Item 1", 10.0, 1, 0.0));
            productManager.addProduct(new Product("Item 2", 20.0, 1, 0.0));
            
            // Remove the first one
            boolean result = productManager.removeProduct(0);
            
            assertTrue(result, "Remove operation should return true for success");
            assertEquals(1, productManager.getInventory().size(), "Inventory should have one product left");
            assertEquals("Item 2", productManager.getInventory().get(0).getName(), "Remaining product should be Item 2");
        }
        
        @Test
        @DisplayName("Find product by name")
        void testFindProductByName() {
            Product product = new Product("Unique Item", 7.5, 3, 0.0);
            productManager.addProduct(product);
            productManager.addProduct(new Product("Other Item", 12.0, 1, 0.0));
            
            Optional<Product> found = productManager.findProductByName("Unique Item");
            
            assertTrue(found.isPresent(), "Product should be found");
            assertEquals("Unique Item", found.get().getName(), "Found product name should match");
            assertEquals(7.5, found.get().getPrice().doubleValue(), 0.001, "Found product price should match");
        }
        
        @Test
        @DisplayName("Calculate total quantities and prices")
        void testTotalCalculations() {
            productManager.addProduct(new Product("Item 1", 10.0, 3, 0.0));
            productManager.addProduct(new Product("Item 2", 5.0, 2, 0.0));
            
            assertEquals(5, productManager.getTotalQuantity(), "Total quantity should be sum of all product quantities");
            assertEquals(40.0, productManager.getTotalGrossPrice().doubleValue(), 0.001,
                    "Gross price should be sum of price × quantity for all products");
        }
        
        @Test
        @DisplayName("Calculate prices with discounts")
        void testDiscountCalculations() {
            productManager.addProduct(new Product("Regular Item", 10.0, 2, 0.1)); // $18.00 after 10% discount
            
            LocalDate soon = LocalDate.now().plusDays(3);
            productManager.addProduct(new PerishableProduct("Perishable Item", 5.0, 2, soon.toString(), 0.0));
            // Perishable item will have additional discount due to expiration date
            
            BigDecimal netPrice = productManager.getTotalNetPriceWithDiscount();
            BigDecimal perishablePrice = productManager.getTotalPriceWithPerishableDiscount();
            
            // Net price should be less than perishable price (due to additional 15% discount)
            assertTrue(netPrice.compareTo(perishablePrice) < 0, 
                    "Net price should be less than price with just perishable discounts");
        }
    }
    
    @AfterEach
    void tearDown() {
        // Clean up after each test
        List<Product> products = productManager.getInventory();
        while (!products.isEmpty()) {
            productManager.removeProduct(0);
            products = productManager.getInventory();
        }
    }
}
