package com.store.gui;

import com.store.model.PerishableProduct;
import com.store.model.Product;
import com.store.service.StoreService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import javafx.beans.property.SimpleObjectProperty;

public class InventoryApp extends Application {

    // Use the singleton instance instead of creating a new one
    private final StoreService storeService = StoreService.getInstance();
    private TableView<Product> productTable;
    private final ObservableList<Product> productData = FXCollections.observableArrayList();

    // Form fields
    private TextField nameField;
    private TextField priceField;
    private TextField quantityField;
    private TextField discountField;
    private CheckBox perishableCheckBox;
    private DatePicker expirationDatePicker;
    
    // Summary labels
    private Label totalQuantityValue;
    private Label totalGrossPriceValue;
    private Label totalPerishablePriceValue;
    private Label totalNetPriceValue;

    @Override
    public void start(Stage primaryStage) {
        try {
            BorderPane root = new BorderPane();
            root.setPadding(new Insets(10));
            
            // Create main sections
            VBox leftPanel = createLeftPanel();
            VBox centerPanel = createCenterPanel();
            HBox topPanel = createTopPanel();
            
            root.setLeft(leftPanel);
            root.setCenter(centerPanel);
            root.setTop(topPanel);
            
            // Load data
            refreshTableData();
            updateSummary();
            
            Scene scene = new Scene(root, 1000, 700);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            primaryStage.setTitle("Store Inventory Management System");
            primaryStage.setScene(scene);
            
            // Add explicit window close handler to save inventory with better error handling
            primaryStage.setOnCloseRequest(event -> {
                System.out.println("Window closing event detected, saving inventory...");
                saveInventoryBeforeExit();
            });
            
            primaryStage.show();
            System.out.println("GUI window displayed successfully");
        } catch (Exception e) {
            System.err.println("Error initializing GUI: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Helper method to save inventory with proper error handling
     */
    private void saveInventoryBeforeExit() {
        try {
            // Try with instance first
            storeService.saveAndCloseInventory();
            System.out.println("Inventory saved successfully on application exit.");
        } catch (Exception e) {
            System.err.println("Error with instance save, trying with singleton: " + e.getMessage());
            try {
                // Fallback to singleton
                StoreService.getInstance().saveAndCloseInventory();
                System.out.println("Inventory saved successfully using singleton.");
            } catch (Exception ex) {
                System.err.println("CRITICAL ERROR: Failed to save inventory: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
    
    @Override
    public void stop() throws Exception {
        // This method is called when the application is stopping
        System.out.println("JavaFX application stop() method called, ensuring inventory is saved...");
        
        // Force save inventory on application stop
        try {
            // Save using singleton - no need to check twice since we're using the singleton already
            if (!productData.isEmpty()) {
                storeService.saveAndCloseInventory();
                System.out.println("GUI inventory data saved: " + productData.size() + " products");
            } else {
                System.out.println("WARNING: Product data is empty! Not overwriting inventory file.");
            }
        } catch (Exception e) {
            System.err.println("Error saving inventory during application stop: " + e.getMessage());
            e.printStackTrace();
        }
        
        super.stop();
    }
    
    private HBox createTopPanel() {
        Label titleLabel = new Label("Store Inventory Management System");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.getStyleClass().add("header-label");
        
        HBox topPanel = new HBox(titleLabel);
        topPanel.setPadding(new Insets(10, 0, 20, 0));
        return topPanel;
    }
    
    private VBox createLeftPanel() {
        VBox leftPanel = new VBox(10);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setMinWidth(250);
        leftPanel.getStyleClass().add("inventory-section");
        
        Label menuTitle = new Label("Menu");
        menuTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        menuTitle.setTextFill(Color.WHITE);
        
        // Create menu buttons
        Button addProductBtn = createMenuButton("Add Product");
        Button viewInventoryBtn = createMenuButton("View Inventory");
        Button searchProductBtn = createMenuButton("Search Product");
        Button displaySummaryBtn = createMenuButton("Display Summary");
        Button removeProductBtn = createMenuButton("Remove Product");
        Button consoleModeBtn = createMenuButton("Switch to Console"); // New button
        Button exitBtn = createMenuButton("Exit");
        
        // Add actions to buttons
        addProductBtn.setOnAction(e -> showAddProductForm());
        viewInventoryBtn.setOnAction(e -> refreshTableData());
        searchProductBtn.setOnAction(e -> showSearchDialog());
        displaySummaryBtn.setOnAction(e -> updateSummary());
        removeProductBtn.setOnAction(e -> removeSelectedProduct());
        consoleModeBtn.setOnAction(e -> switchToConsoleMode()); // New action
        exitBtn.setOnAction(e -> System.exit(0));
        
        // Summary section
        VBox summaryBox = createSummarySection();
        
        leftPanel.getChildren().addAll(menuTitle, addProductBtn, viewInventoryBtn, 
                searchProductBtn, displaySummaryBtn, removeProductBtn, consoleModeBtn, exitBtn,
                new Separator(), summaryBox);
        
        return leftPanel;
    }
    
    private VBox createSummarySection() {
        VBox summaryBox = new VBox(5);
        summaryBox.setPadding(new Insets(10, 0, 0, 0));
        
        Label summaryTitle = new Label("Inventory Summary");
        summaryTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        summaryTitle.setTextFill(Color.WHITE);
        
        // Create summary items
        totalQuantityValue = new Label("0");
        totalGrossPriceValue = new Label("$0.00");
        totalPerishablePriceValue = new Label("$0.00");
        totalNetPriceValue = new Label("$0.00");
        
        // Style labels
        totalQuantityValue.setTextFill(Color.WHITE);
        totalGrossPriceValue.setTextFill(Color.WHITE);
        totalPerishablePriceValue.setTextFill(Color.WHITE);
        totalNetPriceValue.setTextFill(Color.WHITE);
        
        // Create grid for summary
        GridPane summaryGrid = new GridPane();
        summaryGrid.setHgap(5);
        summaryGrid.setVgap(5);
        
        // Add labels to grid
        addSummaryRow(summaryGrid, 0, "Total Quantity:", totalQuantityValue);
        addSummaryRow(summaryGrid, 1, "Total Gross Price:", totalGrossPriceValue);
        addSummaryRow(summaryGrid, 2, "With Perishable Discount:", totalPerishablePriceValue);
        addSummaryRow(summaryGrid, 3, "With 15% Discount:", totalNetPriceValue);
        
        summaryBox.getChildren().addAll(summaryTitle, summaryGrid);
        return summaryBox;
    }
    
    private void addSummaryRow(GridPane grid, int row, String labelText, Label valueLabel) {
        Label label = new Label(labelText);
        label.setTextFill(Color.WHITE);
        grid.add(label, 0, row);
        grid.add(valueLabel, 1, row);
    }
    
    private Button createMenuButton(String text) {
        Button button = new Button(text);
        button.setPrefWidth(230);
        button.setPrefHeight(40);
        return button;
    }
    
    private VBox createCenterPanel() {
        VBox centerPanel = new VBox(10);
        centerPanel.setPadding(new Insets(10));
        
        // Create table view for products
        productTable = new TableView<>();
        productTable.setPlaceholder(new Label("No products in inventory"));
        
        // Define columns
        TableColumn<Product, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(150);
        
        TableColumn<Product, BigDecimal> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(100);
        
        TableColumn<Product, Integer> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityCol.setPrefWidth(100);
        
        TableColumn<Product, BigDecimal> discountCol = new TableColumn<>("Discount");
        discountCol.setCellValueFactory(new PropertyValueFactory<>("discount"));
        discountCol.setPrefWidth(100);
        discountCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal discount, boolean empty) {
                super.updateItem(discount, empty);
                if (empty || discount == null) {
                    setText(null);
                } else {
                    // Format discount as percentage
                    setText(discount.multiply(BigDecimal.valueOf(100)) + "%");
                }
            }
        });
        
        TableColumn<Product, LocalDate> expirationCol = new TableColumn<>("Expiration Date");
        expirationCol.setPrefWidth(150);
        expirationCol.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
            if (product instanceof PerishableProduct) {
                // Create a new SimpleObjectProperty with the expiration date
                return new SimpleObjectProperty<>(((PerishableProduct) product).getExpirationDate());
            }
            return new SimpleObjectProperty<>(null);
        });
        expirationCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(DateTimeFormatter.ISO_LOCAL_DATE));
                }
            }
        });
        
        TableColumn<Product, String> totalValueCol = new TableColumn<>("Total Value");
        totalValueCol.setPrefWidth(120);
        totalValueCol.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
            return new SimpleObjectProperty<>(product.getTotalValue().toString());
        });
        totalValueCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText("$" + value);
                }
            }
        });
        
        // Add all columns to the table (using a safer method to avoid type warnings)
        productTable.getColumns().add(nameCol);
        productTable.getColumns().add(priceCol);
        productTable.getColumns().add(quantityCol);
        productTable.getColumns().add(discountCol);
        productTable.getColumns().add(expirationCol);
        productTable.getColumns().add(totalValueCol);
        
        productTable.setItems(productData);
        
        centerPanel.getChildren().add(productTable);
        VBox.setVgrow(productTable, Priority.ALWAYS);
        
        return centerPanel;
    }
    
    public void showAddProductForm() {
        // Create a dialog
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Add Product");
        dialog.setHeaderText("Enter Product Details");
        
        // Set the button types
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
        
        // Create the form grid pane
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        nameField = new TextField();
        nameField.setPromptText("Product name");
        priceField = new TextField();
        priceField.setPromptText("Price");
        quantityField = new TextField();
        quantityField.setPromptText("Quantity");
        discountField = new TextField();
        discountField.setPromptText("Discount (%)");
        perishableCheckBox = new CheckBox("Perishable Product");
        expirationDatePicker = new DatePicker();
        expirationDatePicker.setPromptText("Expiration Date");
        
        // Show/hide expiration date picker based on checkbox
        expirationDatePicker.setVisible(false);
        expirationDatePicker.setManaged(false);
        
        // Set date converter for expiration date
        expirationDatePicker.setConverter(new StringConverter<>() {
            private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            
            @Override
            public String toString(LocalDate date) {
                return (date != null) ? dateFormatter.format(date) : "";
            }
            
            @Override
            public LocalDate fromString(String string) {
                return (string != null && !string.isEmpty()) ? LocalDate.parse(string, dateFormatter) : null;
            }
        });
        
        // Show/hide expiration date picker based on checkbox
        perishableCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            expirationDatePicker.setVisible(newVal);
            expirationDatePicker.setManaged(newVal);
        });
        
        // Add fields to grid
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Price:"), 0, 1);
        grid.add(priceField, 1, 1);
        grid.add(new Label("Quantity:"), 0, 2);
        grid.add(quantityField, 1, 2);
        grid.add(new Label("Discount (%):"), 0, 3);
        grid.add(discountField, 1, 3);
        grid.add(perishableCheckBox, 0, 4, 2, 1);
        grid.add(new Label("Expiration Date:"), 0, 5);
        grid.add(expirationDatePicker, 1, 5);
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the name field
        nameField.requestFocus();
        
        // Convert the result to a Product when the add button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    String name = nameField.getText().trim();
                    double price = Double.parseDouble(priceField.getText().trim());
                    int quantity = Integer.parseInt(quantityField.getText().trim());
                    double discount = Double.parseDouble(discountField.getText().trim()) / 100.0;
                    
                    if (name.isEmpty()) {
                        showAlert("Error", "Product name cannot be empty.");
                        return null;
                    }
                    
                    if (perishableCheckBox.isSelected()) {
                        LocalDate expirationDate = expirationDatePicker.getValue();
                        if (expirationDate == null) {
                            showAlert("Error", "Please select an expiration date.");
                            return null;
                        }
                        return new PerishableProduct(name, price, quantity, 
                                expirationDate.toString(), discount);
                    } else {
                        return new Product(name, price, quantity, discount);
                    }
                } catch (NumberFormatException e) {
                    showAlert("Error", "Invalid number format. Please check your inputs.");
                    return null;
                }
            }
            return null;
        });
        
        // Show the dialog and process the result
        Optional<Product> result = dialog.showAndWait();
        
        result.ifPresent(product -> {
            storeService.addProduct(product);
            refreshTableData();
            updateSummary();
        });
    }
    
    public void refreshTableData() {
        productData.clear();
        productData.addAll(storeService.getInventory());
    }
    
    public void updateSummary() {
        totalQuantityValue.setText(String.valueOf(storeService.getTotalQuantity()));
        totalGrossPriceValue.setText("$" + storeService.getTotalGrossPrice());
        totalPerishablePriceValue.setText("$" + storeService.getTotalPriceWithPerishableDiscount());
        totalNetPriceValue.setText("$" + storeService.getTotalNetPriceWithDiscount());
    }
    
    private void showSearchDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Search Product");
        dialog.setHeaderText("Enter product name to search");
        dialog.setContentText("Product name:");
        
        Optional<String> result = dialog.showAndWait();
        
        result.ifPresent(name -> {
            Optional<Product> product = storeService.findProductByName(name);
            if (product.isPresent()) {
                // Filter table to show only this product
                productData.clear();
                productData.add(product.get());
            } else {
                showAlert("Product Not Found", "No product found with name: " + name);
                refreshTableData();
            }
        });
    }
    
    private void removeSelectedProduct() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            int index = storeService.getInventory().indexOf(selectedProduct);
            if (index >= 0) {
                storeService.removeProduct(index);
                refreshTableData();
                updateSummary();
            }
        } else {
            showAlert("No Selection", "Please select a product to remove.");
        }
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Switches from GUI mode to console mode
     */
    private void switchToConsoleMode() {
        try {
            // Save inventory before switching
            saveInventoryBeforeExit();
            
            // Show confirmation dialog
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Switch to Console Mode");
            alert.setHeaderText("Mode Change Confirmation");
            alert.setContentText("Do you want to switch to console mode? This will close the current window.");
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Use our static method to notify AppLauncher to switch modes
                com.store.AppLauncher.requestConsoleMode();
                
                // Properly close the JavaFX application
                Stage stage = (Stage) productTable.getScene().getWindow();
                stage.close();
                Platform.exit();
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to switch to console mode: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}