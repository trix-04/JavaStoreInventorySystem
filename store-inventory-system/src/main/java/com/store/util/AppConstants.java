package com.store.util;

/**
 * Central place for application constants
 */
public final class AppConstants {
    // File paths
    public static final String USER_INVENTORY_DIR = ".store-inventory";
    public static final String INVENTORY_FILENAME = "inventory.json";
    public static final String RESOURCES_PATH = "src/main/resources/";
    
    // System properties
    public static final String USER_HOME_PROPERTY = "user.home";
    public static final String USER_DIR_PROPERTY = "user.dir";
    
    // UI related
    public static final String APP_TITLE = "Store Inventory Management System";
    
    // Format patterns
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    
    private AppConstants() {
        // Prevent instantiation
    }
}
