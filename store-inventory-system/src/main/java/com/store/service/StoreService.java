package com.store.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.store.model.Product;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StoreService {
    // Singleton instance
    private static StoreService instance;

    private List<Product> inventory;
    private final ObjectMapper objectMapper;
    
    // File configuration constants
    private static final String SAMPLE_INVENTORY_FILENAME = "inventory.json";
    private static final String RESOURCES_PATH = "src/main/resources/";
    private static final String USER_INVENTORY_DIR = ".store-inventory";
    private static final String USER_INVENTORY_FILENAME = "inventory.json";
    private File inventoryFile;
    
    // Add a fixed reference to the template file
    private static final File TEMPLATE_INVENTORY_FILE = new File(RESOURCES_PATH + SAMPLE_INVENTORY_FILENAME);

    /**
     * Get the singleton instance of StoreService
     */
    public static synchronized StoreService getInstance() {
        if (instance == null) {
            instance = new StoreService();
        }
        return instance;
    }

    /**
     * Private constructor (use getInstance() instead)
     */
    public StoreService() {
        this.inventory = new ArrayList<>();
        
        // Create a polymorphic type validator to allow the Product class hierarchy
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
            .allowIfBaseType(Product.class)
            .build();
            
        // Configure ObjectMapper with the polymorphic type validator
        this.objectMapper = JsonMapper.builder()
            .polymorphicTypeValidator(ptv)
            .build();
            
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        // Initialize inventory file in dedicated directory
        setupUserInventoryFile();
        
        // Load inventory data
        loadInventory();
    }

    /**
     * Sets up the user-specific inventory file in the .store-inventory directory
     * within the user's home directory. Creates the directory if it doesn't exist.
     */
    private void setupUserInventoryFile() {
        try {
            // Get user home directory path
            String userHome = System.getProperty("user.home");
            
            // Create path to .store-inventory directory
            Path userInventoryDirPath = Paths.get(userHome, USER_INVENTORY_DIR);
            
            // Create the directory if it doesn't exist
            if (!Files.exists(userInventoryDirPath)) {
                Files.createDirectory(userInventoryDirPath);
                System.out.println("Created user inventory directory: " + userInventoryDirPath);
            }
            
            // Set up the full path to inventory.json in the .store-inventory directory
            Path userInventoryPath = userInventoryDirPath.resolve(USER_INVENTORY_FILENAME);
            inventoryFile = userInventoryPath.toFile();
            
            System.out.println("Using inventory file at: " + inventoryFile.getAbsolutePath());
            
            // Only create a new inventory file if it doesn't exist
            if (!Files.exists(userInventoryPath)) {
                System.out.println("User inventory file not found. Creating from template...");
                
                // Always use the fixed template path first
                if (TEMPLATE_INVENTORY_FILE.exists()) {
                    // Direct file copy to preserve template format exactly
                    Files.copy(TEMPLATE_INVENTORY_FILE.toPath(), userInventoryPath, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Created user inventory file from template: " + TEMPLATE_INVENTORY_FILE.getAbsolutePath());
                    return;
                }
                
                // Fallbacks if the fixed path doesn't work
                Path resourcePath = Paths.get(RESOURCES_PATH, SAMPLE_INVENTORY_FILENAME);
                System.out.println("Looking for template at: " + resourcePath.toAbsolutePath());
                
                if (Files.exists(resourcePath)) {
                    // Use straight file copy to preserve exact JSON format with all fields
                    Files.copy(resourcePath, userInventoryPath, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Created user inventory file from project resources template.");
                    return;
                } else {
                    System.err.println("ERROR: Could not find the template file at " + resourcePath.toAbsolutePath());
                    
                    // Try classpath resources as fallback
                    try (InputStream is = getClass().getClassLoader().getResourceAsStream(SAMPLE_INVENTORY_FILENAME)) {
                        if (is != null) {
                            // Direct stream copy to preserve format
                            Files.copy(is, userInventoryPath, StandardCopyOption.REPLACE_EXISTING);
                            System.out.println("Created user inventory file from classpath resources.");
                            return;
                        } else {
                            System.err.println("ERROR: Could not find template in classpath resources");
                        }
                    }
                }
                
                // If we get here, we couldn't find the template
                System.err.println("WARNING: Could not find inventory template. Creating minimal inventory.");
                createDefaultInventory();
            } else {
                System.out.println("Existing inventory file found. Using current data.");
            }
        } catch (IOException e) {
            System.err.println("Error setting up user inventory file: " + e.getMessage());
            e.printStackTrace();
            
            // Fall back to project resources as last resort
            try {
                Path fallbackPath = Paths.get(RESOURCES_PATH, SAMPLE_INVENTORY_FILENAME);
                inventoryFile = fallbackPath.toFile();
                System.out.println("Falling back to application resource: " + inventoryFile);
                
                // Create parent directories if they don't exist
                Files.createDirectories(fallbackPath.getParent());
            } catch (IOException ex) {
                System.err.println("Fatal error: Cannot initialize inventory file: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * Creates a default inventory file with the correct format when template is unavailable
     */
    private void createDefaultInventory() throws IOException {
        // Create a correctly formatted minimal inventory with the required "type" field
        String defaultInventory = 
            "[\n" +
            "  {\n" +
            "    \"type\": \"product\",\n" +
            "    \"name\": \"Potatoes\",\n" +
            "    \"price\": 0.79,\n" +
            "    \"quantity\": 60,\n" +
            "    \"discount\": 0.0\n" +
            "  },\n" +
            "  {\n" +
            "    \"type\": \"product\",\n" +
            "    \"name\": \"Onions\",\n" +
            "    \"price\": 0.89,\n" +
            "    \"quantity\": 45,\n" +
            "    \"discount\": 0.05\n" +
            "  }\n" +
            "]";
        
        Path userInventoryPath = inventoryFile.toPath();
        Files.writeString(userInventoryPath, defaultInventory);
        System.out.println("Created default inventory file with correct format.");
    }

    public void addProduct(Product product) {
        inventory.add(product);
        saveInventory();
    }

    public void removeProduct(int index) {
        if (index >= 0 && index < inventory.size()) {
            inventory.remove(index);
            saveInventory();
        } else {
            System.err.println("Invalid product index: " + index);
        }
    }

    public List<Product> getInventory() {
        return new ArrayList<>(inventory);
    }

    public Optional<Product> findProductByName(String name) {
        return inventory.stream()
                .filter(product -> product.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    public int getTotalQuantity() {
        return inventory.stream()
                .mapToInt(Product::getQuantity)
                .sum();
    }

    public BigDecimal getTotalGrossPrice() {
        return inventory.stream()
                .map(product -> product.getPrice().multiply(BigDecimal.valueOf(product.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotalPriceWithPerishableDiscount() {
        return inventory.stream()
                .map(Product::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotalNetPriceWithDiscount() {
        return getTotalPriceWithPerishableDiscount()
                .multiply(BigDecimal.valueOf(0.85))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Reloads inventory data from disk after template refreshes
     */
    public void loadInventory() {
        if (inventoryFile != null && inventoryFile.exists()) {
            try {
                // First try to pre-validate the JSON structure
                if (validateAndFixJsonIfNeeded()) {
                    Product[] products = objectMapper.readValue(inventoryFile, Product[].class);
                    inventory.clear();
                    inventory.addAll(Arrays.asList(products));
                    System.out.println("Inventory loaded successfully from " + inventoryFile.getPath());
                }
            } catch (IOException e) {
                System.err.println("Error loading inventory: " + e.getMessage());
                
                // Attempt to recover from backup instead of creating one
                tryRestoreFromBackup();
            }
        } else {
            System.out.println("No valid inventory file. Starting with empty inventory.");
            inventory = new ArrayList<>();
            saveInventory();
        }
    }
    
    /**
     * Validates the JSON structure and fixes it if needed
     * @return true if validation succeeded or fixing succeeded
     */
    private boolean validateAndFixJsonIfNeeded() {
        try {
            // Read the file content
            String content = Files.readString(inventoryFile.toPath());
            
            // Parse as generic JSON first
            JsonNode rootNode = objectMapper.readTree(content);
            
            if (!rootNode.isArray()) {
                System.err.println("Invalid inventory format: root element is not an array");
                return false;
            }
            
            // Check if this is a file missing type properties
            boolean needsTypeProperty = false;
            for (JsonNode item : rootNode) {
                if (!item.has("type")) {
                    needsTypeProperty = true;
                    break;
                }
            }
            
            if (needsTypeProperty) {
                System.out.println("Detected inventory JSON missing 'type' property. Fixing format...");
                
                // Create a backup first
                Path backupPath = inventoryFile.toPath().resolveSibling(inventoryFile.getName() + ".missing-type.bak");
                Files.copy(inventoryFile.toPath(), backupPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Created backup at " + backupPath);
                
                // Create the fixed JSON with type properties
                StringBuilder fixedJson = new StringBuilder("[\n");
                boolean first = true;
                
                for (JsonNode item : rootNode) {
                    if (!first) {
                        fixedJson.append(",\n");
                    }
                    first = false;
                    
                    fixedJson.append("  {\n");
                    fixedJson.append("    \"type\": \"product\",\n");
                    
                    boolean innerFirst = true;
                    Iterator<Map.Entry<String, JsonNode>> fields = item.fields();
                    
                    while (fields.hasNext()) {
                        Map.Entry<String, JsonNode> field = fields.next();
                        if (!innerFirst) {
                            fixedJson.append(",\n");
                        }
                        innerFirst = false;
                        
                        fixedJson.append("    \"").append(field.getKey()).append("\": ");
                        
                        if (field.getValue().isTextual()) {
                            fixedJson.append("\"").append(field.getValue().asText()).append("\"");
                        } else {
                            fixedJson.append(field.getValue().toString());
                        }
                    }
                    
                    fixedJson.append("\n  }");
                }
                
                fixedJson.append("\n]");
                
                // Write the fixed JSON back
                Files.writeString(inventoryFile.toPath(), fixedJson.toString());
                System.out.println("Fixed inventory file format by adding 'type' property to all products");
                
                return true;
            }
            
            // File is already valid
            return true;
            
        } catch (IOException e) {
            System.err.println("Error validating/fixing inventory JSON: " + e.getMessage());
            return false;
        }
    }

    /**
     * Attempts to restore inventory from a backup file if it exists
     */
    private void tryRestoreFromBackup() {
        // Define backup file path
        Path backupPath = inventoryFile.toPath().resolveSibling(inventoryFile.getName() + ".bak");
        
        if (Files.exists(backupPath)) {
            System.out.println("Attempting to restore inventory from backup file: " + backupPath);
            
            try {
                // Try to load from backup
                Product[] products = objectMapper.readValue(backupPath.toFile(), Product[].class);
                inventory.clear();
                inventory.addAll(Arrays.asList(products));
                System.out.println("Successfully restored inventory from backup file");
                
                // Save the restored data back to the main file
                saveInventory();
            } catch (IOException backupError) {
                System.err.println("Failed to restore from backup: " + backupError.getMessage());
                createEmptyInventory();
            }
        } else {
            System.out.println("No backup file found. Creating new empty inventory");
            createEmptyInventory();
        }
    }
    
    /**
     * Creates an empty inventory with sample products in the correct format
     */
    private void createEmptyInventory() {
        try {
            createDefaultInventory();
            // Reload the inventory after creating the default file
            Product[] products = objectMapper.readValue(inventoryFile, Product[].class);
            inventory.clear();
            inventory.addAll(Arrays.asList(products));
        } catch (IOException e) {
            System.err.println("Error creating empty inventory: " + e.getMessage());
            // Fallback to in-memory inventory
            inventory = new ArrayList<>();
            inventory.add(new Product("Sample Product", 9.99, 10, 0.0));
        }
    }

    /**
     * Saves the current inventory to the user-specific inventory file.
     */
    private void saveInventory() {
        if (inventoryFile != null) {
            try {
                // First check if inventory is empty but file exists with content
                if (inventory.isEmpty() && inventoryFile.exists() && inventoryFile.length() > 10) {
                    System.out.println("WARNING: Attempting to save empty inventory over existing data!");
                    System.out.println("Creating backup before proceeding...");
                    
                    // Create emergency backup
                    Path backupPath = inventoryFile.toPath().resolveSibling(inventoryFile.getName() + ".emergency.bak");
                    Files.copy(inventoryFile.toPath(), backupPath, StandardCopyOption.REPLACE_EXISTING);
                    
                    // Load from the existing file instead of overwriting with empty data
                    try {
                        Product[] products = objectMapper.readValue(inventoryFile, Product[].class);
                        if (products != null && products.length > 0) {
                            inventory.clear();
                            inventory.addAll(Arrays.asList(products));
                            System.out.println("Recovered " + inventory.size() + " products from existing file instead of overwriting.");
                            return; // Exit without saving to prevent data loss
                        }
                    } catch (Exception e) {
                        System.err.println("Could not read existing inventory: " + e.getMessage());
                    }
                }
                
                // Ensure parent directory exists
                Path parent = inventoryFile.toPath().getParent();
                if (parent != null && !Files.exists(parent)) {
                    Files.createDirectories(parent);
                }
                
                // Only save if we actually have data
                if (!inventory.isEmpty()) {
                    // Write using the configured objectMapper
                    objectMapper.writeValue(inventoryFile, inventory);
                    System.out.println("Inventory saved with " + inventory.size() + " products to " + inventoryFile.getPath());
                } else {
                    System.out.println("Skipping save since inventory is empty!");
                }
            } catch (IOException e) {
                System.err.println("Error saving inventory: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("Cannot save inventory: No valid inventory file path.");
        }
    }

    /**
     * Ensures inventory is saved and any file resources are released
     * before application exit
     */
    public void saveAndCloseInventory() {
        try {
            // Check if inventory is empty before saving
            if (inventory.isEmpty()) {
                System.out.println("WARNING: Inventory is empty! Checking if this is correct...");
                
                // Double-check by trying to load from file directly
                if (inventoryFile != null && inventoryFile.exists() && inventoryFile.length() > 10) {
                    try {
                        // Try to load the file to see if it has data we should keep
                        Product[] fileProducts = objectMapper.readValue(inventoryFile, Product[].class);
                        if (fileProducts != null && fileProducts.length > 0) {
                            System.out.println("Found " + fileProducts.length + 
                                " products in file but memory is empty! Preserving file data.");
                            return; // Don't save and overwrite the file data
                        }
                    } catch (Exception e) {
                        System.err.println("Error checking file content: " + e.getMessage());
                    }
                }
            }
            
            // Final save to ensure latest changes are persisted
            saveInventory();
            
            // Log successful cleanup
            System.out.println("Inventory data saved successfully.");
        } catch (Exception e) {
            System.err.println("Error during final inventory save: " + e.getMessage());
        }
    }
}