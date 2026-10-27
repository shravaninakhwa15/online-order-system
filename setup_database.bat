@echo off
echo ===================================================================
echo Setting up Online Order System Database in MySQL
echo Authors: Sonali Makwana (66) ^& Shravani Nakhwa (69) - SYIT1
echo ===================================================================

set /p MYSQL_USER="Enter MySQL Username [default: root]: "
if "%MYSQL_USER%"=="" set MYSQL_USER=root

set /p MYSQL_PASS="Enter MySQL Password: "

echo.
echo Executing sql\schema.sql...
mysql -u %MYSQL_USER% -p%MYSQL_PASS% < sql\schema.sql
if %ERRORLEVEL% NEQ 0 (
    echo [WARNING] 'mysql' command not in PATH directly. Searching standard installation...
    if exist "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" (
        "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u %MYSQL_USER% -p%MYSQL_PASS% < sql\schema.sql
    ) else (
        echo [ERROR] Could not find MySQL client! Please run sql\schema.sql inside MySQL Workbench.
        pause
        exit /b 1
    )
)

echo.
echo Database 'online_order_system' created and populated successfully!
pause
