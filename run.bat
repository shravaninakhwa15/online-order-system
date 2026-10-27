@echo off
echo Starting Online Order System Desktop Application...
if exist online-order-system.jar (
    java -jar online-order-system.jar
) else (
    echo online-order-system.jar not found, running from compiled bin/ classes...
    java -cp "bin;lib/mysql-connector-j-8.0.33.jar" com.order.Main
)
