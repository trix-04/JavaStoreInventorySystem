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
    
    // Static flag to track if console mode is requested after GUI exits
    private static volatile boolean switchToConsoleMode = false;
    
    // Static method for InventoryApp to request console mode
    public static void requestConsoleMode() {
        switchToConsoleMode = true;
    }
    
    public static void main(String[] args) {
        logSystemInfo();
        
        // Register shutdown hook to ensure clean exit in all scenarios
        registerShutdownHook();
        
        // Configure JavaFX module path if needed
        configureJavaFxModulePath();
        
        // Launch in console mode if requested, otherwise try GUI
        if (shouldRunInConsoleMode(args)) {
            System.out.println("Console mode requested. Starting console application...");
            launchConsoleMode(args);
        } else {
            System.out.println("GUI mode active. Starting GUI application...");
            launchGuiMode(args);
            
            // After GUI exits, check if we should switch to console mode
            if (switchToConsoleMode) {
                System.out.println("Switching to console mode after GUI exit...");
                launchConsoleMode(new String[]{"console"});
            }
        }
    }
    
    /**
     * Configure JavaFX module path programmatically if not already set
     */
    private static void configureJavaFxModulePath() {
        // Check if JavaFX module path is already configured
        if (System.getProperty("javafx.module.path") != null) {
            System.out.println("JavaFX module path already configured: " + System.getProperty("javafx.module.path"));
            return;
        }
        
        // Try to find JavaFX SDK in common locations
        String[] possibleJavaFxPaths = {
            // Check user directory
            Paths.get(System.getProperty("user.dir"), "javafx-sdk", "lib").toString(),
            // Check relative paths
            Paths.get("javafx-sdk", "lib").toString(),
            Paths.get("..", "javafx-sdk", "lib").toString(),
            // Check Maven paths
            System.getProperty("user.home") + "/.m2/repository/org/openjfx"
        };
        
        for (String path : possibleJavaFxPaths) {
            File dir = new File(path);
            if (dir.exists() && dir.isDirectory()) {
                // Look for JavaFX JAR files
                File[] files = dir.listFiles((d, name) -> name.startsWith("javafx") && name.endsWith(".jar"));
                if (files != null && files.length > 0) {
                    System.out.println("Found JavaFX libraries at: " + path);
                    System.setProperty("javafx.module.path", path);
                    
                    // Set the module additions if not already set
                    if (System.getProperty("javafx.modules") == null) {
                        System.setProperty("javafx.modules", "javafx.controls,javafx.fxml");
                        System.out.println("Set JavaFX modules: javafx.controls,javafx.fxml");
                    }
                    
                    return;
                }
            }
        }
        
        System.out.println("JavaFX module path not found automatically. If you encounter JavaFX errors, run with:");
        System.out.println("--module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml");
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
    
    /**
     * Forces a refresh by replacing the user inventory with the template inventory
     */
    public static void refreshUserInventoryFromTemplate() {
        System.out.println("Refreshing inventory from template file...");
        try {
            // Use absolute path to ensure template file is found
            String projectDir = System.getProperty("user.dir");
            Path templatePath = Paths.get(projectDir, "src", "main", "resources", "inventory.json");
            File templateFile = templatePath.toFile();
            
            String userHome = System.getProperty("user.home");
            Path userInventoryDir = Paths.get(userHome, ".store-inventory");
            Path userInventoryFile = userInventoryDir.resolve("inventory.json");

            // Ensure directory exists
            if (!Files.exists(userInventoryDir)) {
                Files.createDirectory(userInventoryDir);
                System.out.println("Created user inventory directory: " + userInventoryDir);
            }

            // Check if file exists and ask for confirmation before overwriting
            if (Files.exists(userInventoryFile)) {
                System.out.println("WARNING: An existing inventory file was found at: " + userInventoryFile);
                System.out.print("Do you want to overwrite it with the template? (y/n): ");
                
                try (Scanner scanner = new Scanner(System.in)) {
                    String response = scanner.nextLine().trim().toLowerCase();
                    if (!response.startsWith("y")) {
                        System.out.println("Refresh cancelled. Using existing inventory file.");
                        return;
                    }
                    System.out.println("Proceeding with refresh...");
                }
            }

            // Report on file existence and contents for debugging
            System.out.println("Template file location: " + templatePath);
            if (templateFile.exists()) {
                System.out.println("Template file exists, size: " + Files.size(templatePath) + " bytes");
                System.out.println("Template contains " + Files.readAllLines(templatePath).size() + " lines");
                
                // Perform the copy with explicit replace option
                Files.copy(templatePath, userInventoryFile, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Successfully refreshed user inventory from template.");
            } else {
                System.err.println("ERROR: Template file not found at: " + templateFile.getAbsolutePath());
                System.err.println("Current working directory: " + projectDir);
                
                // List files in resources dir to aid debugging
                Path resourcesDir = Paths.get(projectDir, "src", "main", "resources");
                if (Files.exists(resourcesDir)) {
                    System.out.println("Files in resources directory:");
                    Files.list(resourcesDir).forEach(p -> System.out.println("  - " + p.getFileName()));
                }
            }
        } catch (IOException e) {
            System.err.println("Error refreshing inventory: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static boolean shouldForceRefresh(String[] args) {
        for (String arg : args) {
            if (arg.equalsIgnoreCase("--refresh") || arg.equalsIgnoreCase("-r")) {
                return true;
            }
        }
        return false;
    }
    
    private static void launchConsoleMode(String[] args) {
        try {
            // Check if a refresh is requested
            if (shouldForceRefresh(args)) {
                refreshUserInventoryFromTemplate();
            }
            
            // Make sure StoreService is initialized with correct template path before Main runs
            StoreService.getInstance();
            System.out.println("StoreService initialized with template from resources directory");
            
            // Console mode cleanup is handled by Main.java
            Main.main(args);
        } catch (Exception e) {
            System.err.println("Error in console mode: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void launchGuiMode(String[] args) {
        try {
            // IMPORTANT: Initialize StoreService FIRST before checking for refresh
            StoreService service = StoreService.getInstance();
            System.out.println("StoreService initialized with template from resources directory");
            
            // Only after loading, check if a refresh is requested
            if (shouldForceRefresh(args)) {
                // Create a backup of current inventory before refreshing
                createInventoryBackup();
                refreshUserInventoryFromTemplate();
                // Re-initialize StoreService to reload the refreshed data
                service.loadInventory(); // Reload inventory data after refresh
            }
            
            System.out.println("Starting GUI application...");
            
            // Set up JavaFX exit handler for clean shutdown
            Platform.setImplicitExit(true);
            
            // Check for JavaFX module configuration before launching
            checkJavaFxConfiguration();
            
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
    
    /**
     * Verify JavaFX configuration before launching the application
     */
    private static void checkJavaFxConfiguration() {
        try {
            // Verify JavaFX classes are accessible
            Class.forName("javafx.scene.Scene");
            Class.forName("javafx.fxml.FXMLLoader");
            
            // Check if modules are properly configured by testing a simple JavaFX operation
            if (System.getProperty("javafx.module.path") == null) {
                System.out.println("WARNING: JavaFX module path not explicitly set. This may cause issues.");
                System.out.println("If you encounter 'Unsupported JavaFX configuration' errors, use:");
                System.out.println("java --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml -jar your-app.jar");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("ERROR: JavaFX classes not found. Please check your module configuration.");
            System.err.println("Required JavaFX modules: javafx.controls, javafx.fxml");
            throw new RuntimeException("JavaFX configuration error: " + e.getMessage(), e);
        }
    }
    
    /**
     * Create a backup of the current inventory file before refresh
     */
    private static void createInventoryBackup() {
        try {
            String userHome = System.getProperty("user.home");
            Path userInventoryDir = Paths.get(userHome, ".store-inventory");
            Path userInventoryFile = userInventoryDir.resolve("inventory.json");
            
            if (Files.exists(userInventoryFile)) {
                Path backupFile = userInventoryDir.resolve("inventory.json.bak");
                Files.copy(userInventoryFile, backupFile, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Created backup of current inventory at " + backupFile);
            }
        } catch (IOException e) {
            System.err.println("Failed to create inventory backup: " + e.getMessage());
        }
    }
}
