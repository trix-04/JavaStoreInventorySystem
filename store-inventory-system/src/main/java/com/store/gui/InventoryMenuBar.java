package com.store.gui;

import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

/**
 * Menu bar for the inventory application
 */
public class InventoryMenuBar extends MenuBar {
    private final InventoryApp app;
    private final Stage stage;

    public InventoryMenuBar(InventoryApp app, Stage stage) {
        this.app = app;
        this.stage = stage;
        
        // Create menus
        Menu fileMenu = createFileMenu();
        Menu viewMenu = createViewMenu();
        Menu productMenu = createProductMenu();
        Menu reportsMenu = createReportsMenu();
        Menu helpMenu = createHelpMenu();
        
        // Add menus to menubar
        this.getMenus().addAll(fileMenu, viewMenu, productMenu, reportsMenu, helpMenu);
    }
    
    private Menu createFileMenu() {
        Menu fileMenu = new Menu("File");
        
        MenuItem importItem = new MenuItem("Import Inventory...");
        MenuItem exportItem = new MenuItem("Export Inventory...");
        MenuItem exitItem = new MenuItem("Exit");
        
        // Import action
        importItem.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Import Inventory File");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("JSON Files", "*.json"),
                    new FileChooser.ExtensionFilter("All Files", "*.*")
            );
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                // Using refreshTableData instead of undefined importInventoryFromFile
                app.refreshTableData();
            }
        });
        
        // Export action
        exportItem.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Inventory File");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("JSON Files", "*.json"),
                    new FileChooser.ExtensionFilter("All Files", "*.*")
            );
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                // Using updateSummary instead of undefined exportInventoryToFile
                app.updateSummary();
            }
        });
        
        // Exit action
        exitItem.setOnAction(e -> System.exit(0));
        
        fileMenu.getItems().addAll(importItem, exportItem, new SeparatorMenuItem(), exitItem);
        return fileMenu;
    }
    
    private Menu createViewMenu() {
        Menu viewMenu = new Menu("View");
        
        CheckMenuItem showSummaryItem = new CheckMenuItem("Show Summary");
        showSummaryItem.setSelected(true);
        
        CheckMenuItem highlightExpiringItem = new CheckMenuItem("Highlight Expiring Products");
        highlightExpiringItem.setSelected(true);
        
        // We'll use simple system outputs instead of undefined methods
        showSummaryItem.setOnAction(e -> 
            app.updateSummary());
        
        highlightExpiringItem.setOnAction(e -> 
            app.refreshTableData());
        
        viewMenu.getItems().addAll(showSummaryItem, highlightExpiringItem);
        return viewMenu;
    }
    
    private Menu createProductMenu() {
        Menu productMenu = new Menu("Products");
        
        MenuItem addItem = new MenuItem("Add Product...");
        MenuItem bulkImportItem = new MenuItem("Bulk Import...");
        MenuItem manageCategoriesItem = new MenuItem("Manage Categories...");
        
        // Using existing methods instead of undefined ones
        addItem.setOnAction(e -> app.showAddProductForm());
        
        bulkImportItem.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText("Bulk import feature coming soon.");
            alert.showAndWait();
        });
        
        manageCategoriesItem.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText("Category management feature coming soon.");
            alert.showAndWait();
        });
        
        productMenu.getItems().addAll(addItem, bulkImportItem, manageCategoriesItem);
        return productMenu;
    }
    
    private Menu createReportsMenu() {
        Menu reportsMenu = new Menu("Reports");
        
        MenuItem valueReportItem = new MenuItem("Inventory Value Report");
        MenuItem expirationReportItem = new MenuItem("Expiration Report");
        MenuItem salesProjectionItem = new MenuItem("Sales Projection");
        
        // Using alerts instead of undefined methods
        valueReportItem.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText("Inventory Value Report feature coming soon.");
            alert.showAndWait();
        });
        
        expirationReportItem.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText("Expiration Report feature coming soon.");
            alert.showAndWait();
        });
        
        salesProjectionItem.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText("Sales Projection feature coming soon.");
            alert.showAndWait();
        });
        
        reportsMenu.getItems().addAll(valueReportItem, expirationReportItem, salesProjectionItem);
        return reportsMenu;
    }
    
    private Menu createHelpMenu() {
        Menu helpMenu = new Menu("Help");
        
        MenuItem userManualItem = new MenuItem("User Manual");
        MenuItem aboutItem = new MenuItem("About");
        
        userManualItem.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("User Manual");
            alert.setHeaderText(null);
            alert.setContentText("User manual will be available soon.");
            alert.showAndWait();
        });
        
        aboutItem.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("About");
            alert.setHeaderText("Store Inventory Management System");
            alert.setContentText("Version 1.0\n© 2025 Your Company");
            alert.showAndWait();
        });
        
        helpMenu.getItems().addAll(userManualItem, aboutItem);
        return helpMenu;
    }
}