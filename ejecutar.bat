@echo off
setlocal
cd /d "%~dp0"
if not exist "target\mazmorras-unsa-1.0.0.jar" (
    echo Primero ejecuta compilar.bat o mvn clean package.
    pause
    exit /b 1
)
java -jar "target\mazmorras-unsa-1.0.0.jar"
if errorlevel 1 pause