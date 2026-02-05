@echo off
echo ═══════════════════════════════════════════════════
echo   LAUNCHING SCREENLOG WITH INTERCEPTOR
echo ═══════════════════════════════════════════════════
echo.
echo Mode: STEALTH
echo Status: Loading...
echo.

cd /d C:\JavaProjects\ScreenLogInterceptor

java -javaagent:C:\JavaProjects\ScreenLogInterceptor\target\screenlog-interceptor-1.0.0.jar --module-path C:\Users\lucak\Desktop\ScreenLog\app\lib --add-modules javafx.controls,javafx.fxml -Dinterceptor.mode=stealth -jar C:\Users\lucak\Desktop\ScreenLog\app\screenlog-1.0.0.jar

pause