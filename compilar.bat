@echo off
setlocal
cd /d "%~dp0"
call mvn clean package
if errorlevel 1 (
    echo La compilacion fallo. Revisa los mensajes ERROR anteriores.
    pause
    exit /b 1
)
echo Compilacion terminada. JAR en target\mazmorras-unsa-1.0.0.jar
pause