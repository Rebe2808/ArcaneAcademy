@echo off
setlocal
cd /d "%~dp0"
if not exist test-build mkdir test-build
dir /s /b src\*.java > test-fuentes.txt
javac -encoding UTF-8 -d test-build @test-fuentes.txt
if errorlevel 1 exit /b 1
javac -encoding UTF-8 -cp test-build -d test-build tests\PruebasPOO.java tests\PruebasVentana.java tests\PruebasSprites.java
if errorlevel 1 exit /b 1
java -Djava.awt.headless=true -cp test-build PruebasPOO
if errorlevel 1 exit /b 1
java -cp test-build PruebasVentana
if errorlevel 1 exit /b 1
java -Djava.awt.headless=true -cp test-build PruebasSprites
if errorlevel 1 exit /b 1
echo Todas las pruebas pasaron.
pause
