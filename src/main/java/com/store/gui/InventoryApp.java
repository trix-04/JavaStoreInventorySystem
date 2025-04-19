// Replace your existing switchToConsoleMode method with this:
private void switchToConsoleMode() {
    try {
        // Save inventory changes before switching modes
        saveInventoryBeforeExit();
        
        // Create a confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Switch to console mode? Current window will close.", 
                ButtonType.YES, ButtonType.NO);
        alert.setTitle("Switch to Console Mode");
        alert.setHeaderText("Mode Change Confirmation");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            // Signal to AppLauncher that we want to switch to console mode
            com.store.AppLauncher.requestConsoleMode();
            
            // Get the current stage and close it to trigger JavaFX shutdown
            Stage stage = (Stage) productTable.getScene().getWindow();
            stage.close();
        }
    } catch (Exception e) {
        showAlert("Error", "Failed to switch to console mode: " + e.getMessage());
    }
}