@echo off
echo ═══════════════════════════════════════════════════
echo   ATTACK MODE: REDACT
echo ═══════════════════════════════════════════════════
cd /d C:\JavaProjects\ScreenLogInterceptor
java -javaagent:C:\JavaProjects\ScreenLogInterceptor\target\screenlog-interceptor-1.0.0.jar --module-path C:\Users\lucak\Desktop\ScreenLog\app\lib --add-modules javafx.controls,javafx.fxml -Dinterceptor.mode=redact -Dinterceptor.redact.regions="1720,0,200,50;100,50,1000,30" -jar C:\Users\lucak\Desktop\ScreenLog\app\screenlog-1.0.0.jar
pause