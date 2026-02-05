@echo off
echo ═══════════════════════════════════════════════════
echo   ATTACK MODE: FAKE TIMESTAMP
echo ═══════════════════════════════════════════════════
cd /d C:\JavaProjects\ScreenLogInterceptor
java -javaagent:C:\JavaProjects\ScreenLogInterceptor\target\screenlog-interceptor-1.0.0.jar --module-path C:\Users\lucak\Desktop\ScreenLog\app\lib --add-modules javafx.controls,javafx.fxml -Dinterceptor.mode=timestamp -Dinterceptor.fake.time="2026-02-05 09:00:00" -jar C:\Users\lucak\Desktop\ScreenLog\app\screenlog-1.0.0.jar
pause