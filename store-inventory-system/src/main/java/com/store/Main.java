package com.store;

import com.store.model.Product;
import com.store.model.PerishableProduct;
import com.store.service.StoreService;

import java.math.BigDecimal;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class Main {
    private static final StoreService storeService = new StoreService();
    private static final Scanner scanner = new Scanner(System.in);
    private static final int MAX_RETRY_ATTEMPTS = 3;

    // ANSI color codes for terminal output
    private static final String RESET = "\u001B[0m";
    private static final String BLUE = "\u001B[34m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";

    public static void main(String[] args) {
        clearScreen();
        System.out.println(BLUE + "Welcome to Store Inventory Management System" + RESET);

        // Check if force refresh is requested via args
        if (args.length > 0 && args[0].equalsIgnoreCase("--refresh")) {
            refreshInventoryFromTemplate();
        }

        boolean running = true;
        try {
            while (running) {
                displayMenu();
                int choice = getIntInput("Enter your selection: ", 1, 6);

                switch (choice) {
                    case 1:
                        addProduct();
                        break;
                    case 2:
                        viewInventory();
                        break;
                    case 3:
                        searchProduct();
                        break;
                    case 4:
                        displaySummary();
                        break;
                    case 5:
                        removeProduct();
                        break;
                    case 6:
                        running = false;
                        System.out.println("Saving inventory and exiting...");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        } finally {
            // Ensure resources are properly released on exit
            shutdown();
        }
    }

    /**
     * Forces a refresh from the template inventory with user confirmation if file exists
     */
    private static void refreshInventoryFromTemplate() {
        System.out.println("Refreshing inventory from template file...");
        try {
            // Use absolute path to ensure template file is found
            String projectDir = System.getProperty("user.dir");
            Path templatePath = Paths.get(projectDir, "src", "main", "resources", "inventory.json");
            File templateFile = templatePath.toFile();
            
            String userHome = System.getProperty("user.home");
            Path userInventoryDir = Paths.get(userHome, ".store-inventory");
            Path userInventoryFile = userInventoryDir.resolve("inventory.json");

            // Check if we need to create the directory
            if (!Files.exists(userInventoryDir)) {
                Files.createDirectory(userInventoryDir);
                System.out.println("Created user inventory directory: " + userInventoryDir);
            }

            // Check if file exists and ask for confirmation before overwriting
            if (Files.exists(userInventoryFile)) {
                System.out.println("WARNING: An existing inventory file was found at: " + userInventoryFile);
                System.out.print("Do you want to overwrite it with the template? (y/n): ");
                String response = scanner.nextLine().trim().toLowerCase();
                if (!response.startsWith("y")) {
                    System.out.println("Refresh cancelled. Using existing inventory file.");
                    return;
                }
                System.out.println("Proceeding with refresh...");
            }

            // Copy the template
            if (templateFile.exists()) {
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

    private static void displayMenu() {
        clearScreen();
        printHeader();

        // Display inventory status
        int itemCount = storeService.getInventory().size();
        BigDecimal totalValue = storeService.getTotalGrossPrice();  // Changed from double to BigDecimal
        System.out.println("┌─────────────────────────────────────────────────────┐");
        System.out.printf("│ %s Current Inventory: %d items (Total value: $%s) %s│%n",
                YELLOW, itemCount, totalValue.toString(), RESET);  // Changed %.2f to %s
        System.out.println("└─────────────────────────────────────────────────────┘\n");

        // Main menu options
        System.out.println("┌─────────────────────────────────────────────────────┐");
        System.out.println("│              " + GREEN + "INVENTORY MANAGEMENT MENU" + RESET + "              │");
        System.out.println("├─────────────────────────────────────────────────────┤");
        System.out.println("│  " + CYAN + "1." + RESET + " Add New Product        " + CYAN + "2." + RESET + " View All Inventory    │");
        System.out.println("│  " + CYAN + "3." + RESET + " Search for Product     " + CYAN + "4." + RESET + " Display Summary       │");
        System.out.println("│  " + CYAN + "5." + RESET + " Remove Product         " + CYAN + "6." + RESET + " Exit Application      │");
        System.out.println("└─────────────────────────────────────────────────────┘");
    }

    private static void printHeader() {
        System.out.println(BLUE + "  ____  _                   _                      _                 " + RESET);
        System.out.println(BLUE + " / ___|| |_ ___  _ __ ___  (_)_ ____   _____ _ __ | |_ ___  _ __ _   _ " + RESET);
        System.out.println(BLUE + " \\___ \\| __/ _ \\| '__/ _ \\ | | '_ \\ \\ / / _ \\ '_ \\| __/ _ \\| '__| | | |" + RESET);
        System.out.println(BLUE + "  ___) | || (_) | | |  __/ | | | | \\ V /  __/ | | | || (_) | |  | |_| |" + RESET);
        System.out.println(BLUE + " |____/ \\__\\___/|_|  \\___| |_|_| |_|\\_/ \\___|_| |_|\\__\\___/|_|   \\__, |" + RESET);
        System.out.println(BLUE + "                                                                 |___/ " + RESET);
        System.out.println();
    }

    private static void clearScreen() {
        // Try to clear console screen for better UX
        try {
            String operatingSystem = System.getProperty("os.name");
            if (operatingSystem.contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            // Fall back to printing newlines if clearing fails
            System.out.println("\n\n\n\n\n\n\n\n\n\n");
        }
    }

    private static void addProduct() {
        clearScreen();
        System.out.println(GREEN + "┌─────────────────────────────────┐" + RESET);
        System.out.println(GREEN + "│        ADD NEW PRODUCT          │" + RESET);
        System.out.println(GREEN + "└─────────────────────────────────┘" + RESET);
        System.out.println();

        String name = getStringInput("Enter product name: ");
        double price = getDoubleInput("Enter price: ", 0.01, Double.MAX_VALUE);
        int quantity = getIntInput("Enter quantity: ", 1, Integer.MAX_VALUE);
        double discount = getDoubleInput("Enter discount (%): ", 0, 100) / 100;

        System.out.print("Is this a perishable product? (y/n): ");
        if (scanner.nextLine().trim().toLowerCase().startsWith("y")) {
            String expirationDate = getDateInput("Enter expiration date (YYYY-MM-DD): ");
            storeService.addProduct(new PerishableProduct(name, price, quantity, expirationDate, discount));
        } else {
            storeService.addProduct(new Product(name, price, quantity, discount));
        }
        System.out.println(GREEN + "Product added successfully!" + RESET);
        pressEnterToContinue();
    }

    private static void viewInventory() {
        clearScreen();
        System.out.println(GREEN + "┌─────────────────────────────────┐" + RESET);
        System.out.println(GREEN + "│       CURRENT INVENTORY         │" + RESET);
        System.out.println(GREEN + "└─────────────────────────────────┘" + RESET);
        System.out.println();

        var inventory = storeService.getInventory();

        if (inventory.isEmpty()) {
            System.out.println(YELLOW + "Inventory is empty." + RESET);
            pressEnterToContinue();
            return;
        }

        System.out.println("┌─────┬────────────────────────────────────────────────────────────────────────────────────────────────────────┐");
        System.out.println("│ ID  │ Product Details                                                                                        │");
        System.out.println("├─────┼────────────────────────────────────────────────────────────────────────────────────────────────────────┤");
        
        for (int i = 0; i < inventory.size(); i++) {
            String formattedProduct = formatProductDisplay(inventory.get(i).toString());
            System.out.printf("│ %3d │ %-102s │%n", i, formattedProduct);
        }

        System.out.println("└─────┴────────────────────────────────────────────────────────────────────────────────────────────────────────┘");
        pressEnterToContinue();
    }

    private static String formatProductDisplay(String productString) {
        // Format product display to fit within table width - increased to 100 characters
        if (productString.length() > 100) {
            return productString.substring(0, 97) + "...";
        }
        return productString;
    }

    private static void searchProduct() {
        clearScreen();
        System.out.println(GREEN + "┌─────────────────────────────────┐" + RESET);
        System.out.println(GREEN + "│        SEARCH PRODUCTS          │" + RESET);
        System.out.println(GREEN + "└─────────────────────────────────┘" + RESET);
        System.out.println();
        
        String name = getStringInput("Enter product name to search: ");
        System.out.println("\nSearch Results:");
        System.out.println("---------------");
        storeService.findProductByName(name)
                .ifPresentOrElse(
                        product -> System.out.println(product),
                        () -> System.out.println(YELLOW + "Product not found." + RESET)
                );
        
        pressEnterToContinue();
    }

    private static void displaySummary() {
        clearScreen();
        System.out.println(GREEN + "┌─────────────────────────────────┐" + RESET);
        System.out.println(GREEN + "│       INVENTORY SUMMARY         │" + RESET);
        System.out.println(GREEN + "└─────────────────────────────────┘" + RESET);
        System.out.println();
        
        System.out.println("Total Quantity: " + storeService.getTotalQuantity());
        System.out.println("Total Gross Price: $" + storeService.getTotalGrossPrice());
        System.out.println("Total Price With Perishable Discount: $" + storeService.getTotalPriceWithPerishableDiscount());
        System.out.println("Total Price with additional 15% discount: $" + storeService.getTotalNetPriceWithDiscount());
        
        pressEnterToContinue();
    }

    private static void removeProduct() {
        viewInventory();
        var inventory = storeService.getInventory();

        if (inventory.isEmpty()) {
            return;
        }

        int index = getIntInput("Enter the index of the product to remove: ", 0, inventory.size() - 1);
        storeService.removeProduct(index);
        System.out.println(GREEN + "Product removed successfully!" + RESET);
        pressEnterToContinue();
    }

    private static String getStringInput(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        while (input.isEmpty()) {
            System.out.println("Input cannot be empty. Please try again.");
            System.out.print(prompt);
            input = scanner.nextLine().trim();
        }
        return input;
    }

    private static int getIntInput(String prompt, int min, int max) {
        int attempt = 0;
        while (attempt < MAX_RETRY_ATTEMPTS) {
            try {
                System.out.print(prompt);
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value >= min && value <= max) {
                    return value;
                } else {
                    System.out.printf("Please enter a value between %d and %d.%n", min, max);
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
            attempt++;
            if (attempt == MAX_RETRY_ATTEMPTS) {
                System.out.println("Maximum retry attempts reached. Using default value.");
                return min;
            }
        }
        return min; // Default value
    }

    @SuppressWarnings("unused") // Utility method that might be used in the future
    private static int getIntInput(String prompt) {
        return getIntInput(prompt, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    private static double getDoubleInput(String prompt, double min, double max) {
        int attempt = 0;
        while (attempt < MAX_RETRY_ATTEMPTS) {
            try {
                System.out.print(prompt);
                double value = Double.parseDouble(scanner.nextLine().trim());
                if (value >= min && value <= max) {
                    return value;
                } else {
                    System.out.printf("Please enter a value between %.2f and %.2f.%n", min, max);
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
            attempt++;
            if (attempt == MAX_RETRY_ATTEMPTS) {
                System.out.println("Maximum retry attempts reached. Using default value.");
                return min;
            }
        }
        return min; // Default value
    }

    @SuppressWarnings("unused") // Utility method that might be used in the future
    private static double getDoubleInput(String prompt) {
        return getDoubleInput(prompt, Double.MIN_VALUE, Double.MAX_VALUE);
    }

    private static String getDateInput(String prompt) {
        int attempt = 0;
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        while (attempt < MAX_RETRY_ATTEMPTS) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                LocalDate date = LocalDate.parse(input, formatter);

                // Validate that date is not in the past
                if (date.isBefore(LocalDate.now())) {
                    System.out.println("Warning: Date is in the past.");
                }

                return input;
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use YYYY-MM-DD format.");
            }
            attempt++;
            if (attempt == MAX_RETRY_ATTEMPTS) {
                System.out.println("Maximum retry attempts reached. Using today's date.");
                return LocalDate.now().format(formatter);
            }
        }
        return LocalDate.now().format(formatter); // Default value
    }

    private static void pressEnterToContinue() {
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    /**
     * Properly releases resources before application exit
     */
    private static void shutdown() {
        if (scanner != null) {
            scanner.close();
            System.out.println("Scanner closed.");
        }
        
        if (storeService != null) {
            storeService.saveAndCloseInventory();
            System.out.println("Inventory saved and file resources released.");
        }
        
        System.out.println("Goodbye!");
    }
}
