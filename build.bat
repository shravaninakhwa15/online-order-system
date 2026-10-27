@echo off
setlocal enabledelayedexpansion

echo ===================================================================
echo Building Online Order System (Java Swing + MySQL)
echo Authors: Sonali Makwana (66) ^& Shravani Nakhwa (69) - SYIT1
echo ===================================================================

if not exist bin mkdir bin

echo 1. Gathering Java source files...
dir /s /b src\*.java > sources.txt

echo 2. Compiling Java sources with javac...
javac -encoding UTF-8 -cp "lib/mysql-connector-j-8.0.33.jar" -d bin @sources.txt
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed!
    if exist sources.txt del sources.txt
    pause
    exit /b %ERRORLEVEL%
)
if exist sources.txt del sources.txt

echo 3. Creating Manifest file...
(
    echo Main-Class: com.order.Main
    echo Class-Path: lib/mysql-connector-j-8.0.33.jar
    echo.
) > manifest.txt

echo 4. Locating JAR utility...
set JAR_BIN=jar
where jar >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    if exist "C:\Program Files\Java\jdk-25.0.4\bin\jar.exe" (
        set "JAR_BIN=C:\Program Files\Java\jdk-25.0.4\bin\jar.exe"
    ) else if defined JAVA_HOME (
        if exist "%JAVA_HOME%\bin\jar.exe" set "JAR_BIN=%JAVA_HOME%\bin\jar.exe"
    )
)

echo 5. Packaging runnable JAR (online-order-system.jar)...
"!JAR_BIN!" cfm online-order-system.jar manifest.txt -C bin .
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] JAR packaging failed!
    if exist manifest.txt del manifest.txt
    pause
    exit /b %ERRORLEVEL%
)
if exist manifest.txt del manifest.txt

echo ===================================================================
echo BUILD SUCCESSFUL!
echo Output: online-order-system.jar
echo Run using: run.bat or java -jar online-order-system.jar
echo ===================================================================
pause
