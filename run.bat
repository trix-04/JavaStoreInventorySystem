@echo off
echo Store Inventory Management System
echo ===============================
echo.

:: Check if JavaFX SDK exists
if exist "javafx-sdk\lib" (
    echo Using local JavaFX SDK
    set JAVAFX_PATH=javafx-sdk\lib
    goto run
) else (
    echo Local JavaFX SDK not found, checking other locations...
    
    :: Check for JavaFX in Maven repository
    if exist "%USERPROFILE%\.m2\repository\org\openjfx" (
        echo Using Maven JavaFX modules
        set JAVAFX_PATH=%USERPROFILE%\.m2\repository\org\openjfx
        goto run
    ) else (
        echo WARNING: JavaFX libraries not found.
        echo The application will attempt to run but may fail.
        echo.
        goto run_without_javafx
    )
)

:run
echo Starting application with JavaFX module path: %JAVAFX_PATH%
echo.
java --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.fxml -jar store-inventory-system\target\store-inventory-system-1.0-SNAPSHOT.jar %*
goto end

:run_without_javafx
echo Starting application without JavaFX module path configuration.
echo If you encounter JavaFX errors, please install JavaFX SDK.
echo.
java -jar store-inventory-system\target\store-inventory-system-1.0-SNAPSHOT.jar %*

:end
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Application exited with error level: %ERRORLEVEL%
    pause
) else (
    echo.
    echo Application completed successfully.
)
