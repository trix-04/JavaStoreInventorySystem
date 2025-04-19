@echo off
echo Running Store Inventory System with JavaFX configuration...

set JAVAFX_SDK_PATH=.\javafx-sdk
set MAIN_CLASS=com.store.AppLauncher

echo Using JavaFX SDK from: %JAVAFX_SDK_PATH%
echo Main class: %MAIN_CLASS%

java --module-path "%JAVAFX_SDK_PATH%\lib" --add-modules=javafx.controls,javafx.fxml -cp target\store-inventory-system-1.0-SNAPSHOT.jar %MAIN_CLASS% %*

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Application exited with errors. Error code: %ERRORLEVEL%
    pause
)
