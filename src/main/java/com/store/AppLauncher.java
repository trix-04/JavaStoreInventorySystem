package com.store;

import com.store.gui.InventoryApp;
import com.store.service.StoreService;
import javafx.application.Application;
import javafx.application.Platform;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;

/**
 * Application launcher that determines whether to run in GUI or console mode.
 */
public class AppLauncher {
    
    // Add a flag to track mode changes
    private static volatile boolean switchToConsole = false;
    
    public static void main(String[] args) {
        logSystemInfo();
        
        // Register shutdown hook to ensure clean exit in all scenarios
        registerShutdownHook();
        
        // Launch in console mode if requested, otherwise try GUI
        if (shouldRunInConsoleMode(args)) {
            System.out.println("Console mode requested. Starting console application...");
            launchConsoleMode(args);
        } else {
            System.out.println("GUI mode active. Starting GUI application...");
            launchGuiMode(args);
            
            // After GUI exits, check if we need to switch to console mode
            if (switchToConsole) {
                System.out.println("Switching to console mode after GUI exit...");
                launchConsoleMode(new String[]{"console"});
            }
        }
    }
    
    // Make this accessible from InventoryApp to request mode switch
    public static void requestConsoleMode() {
        switchToConsole = true;
    }
    
    private static void logSystemInfo() {
        System.out.println("Java version: " + System.getProperty("java.version"));
        detectJavaFxRuntime();
    }
    
    private static void detectJavaFxRuntime() {
        // Use existing runtime property if available
        if (System.getProperty("javafx.runtime.version") != null) {
            System.out.println("JavaFX runtime: " + System.getProperty("javafx.runtime.version"));
            return;
        }
        
        try {
            // Check for JavaFX availability
            Class.forName("javafx.application.Application");
            
            // Get version from package if available, otherwise use default
            Package pkg = Application.class.getPackage();
            String version = (pkg != null && pkg.getImplementationVersion() != null) 
                ? pkg.getImplementationVersion() : "21.0.1";
            
            System.setProperty("javafx.runtime.version", version);
            System.out.println("JavaFX runtime: " + version);
        } catch (ClassNotFoundException e) {
            System.out.println("JavaFX runtime: Not available (modules not properly loaded)");
            System.out.println("Check module path configuration with --module-path pointing to JavaFX SDK");
        }
    }
    
    private static boolean shouldRunInConsoleMode(String[] args) {
        // Explicitly check for console mode argument
        if (args != null && args.length > 0) {
            String mode = args[0].toLowerCase();
            return mode.equals("console") || mode.equals("--console") || mode.equals("-c");
        }
        return false; // Default to GUI mode when no args provided
    }
    
    /**
     * Register a JVM shutdown hook to ensure resources are released
     * even if the application is terminated unexpectedly
     */
    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Application shutting down, cleaning up resources...");
            cleanupResources();
        }));
    }
    
    /**
     * Clean up any application-wide resources that need to be released
     */
    private static void cleanupResources() {
        try {
            // Initialize StoreService if not already done to ensure proper template loading
            StoreService service = StoreService.getInstance();
            if (service != null) {
                // Re-enable saving in AppLauncher
                service.saveAndCloseInventory();
                System.out.println("Inventory saved and file resources released.");
            }
        } catch (Exception e) {
            System.err.println("Error during final inventory cleanup: " + e.getMessage());
        }
    }
    
    // Rest of the methods remain the same...
    
    private static void launchGuiMode(String[] args) {
        try {
            // IMPORTANT: Initialize StoreService FIRST before checking for refresh
            // This ensures existing data is loaded before any refresh decision
            StoreService service = StoreService.getInstance();
            System.out.println("StoreService initialized with template from resources directory");
            
            // Only after loading, check if a refresh is requested
            if (shouldForceRefresh(args)) {
                // Create a backup of current inventory before refreshing
                createInventoryBackup();
                refreshUserInventoryFromTemplate();
                // Re-initialize StoreService to load the refreshed data
                service = StoreService.getInstance();
            }
            
            System.out.println("Starting GUI application...");
            
            // Set up JavaFX exit handler for clean shutdown
            Platform.setImplicitExit(true);
            
            // Launch the GUI application
            System.out.println("Launching JavaFX application...");
            Application.launch(InventoryApp.class, args);
        } catch (Exception e) {
            System.err.println("Failed to launch JavaFX application: " + e.getMessage());
            System.err.println("Falling back to console mode...");
            e.printStackTrace();
            launchConsoleMode(args);
        }
    }
    
    // Include all your other methods...
    // (refreshUserInventoryFromTemplate, shouldForceRefresh, createInventoryBackup, etc.)
}