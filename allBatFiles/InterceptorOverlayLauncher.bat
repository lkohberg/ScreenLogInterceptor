@echo off
echo ═══════════════════════════════════════════════════
echo   ATTACK MODE: OVERLAY
echo ═══════════════════════════════════════════════════
cd /d C:\JavaProjects\ScreenLogInterceptor
java -javaagent:C:\JavaProjects\ScreenLogInterceptor\target\screenlog-interceptor-1.0.0.jar --module-path C:\Users\lucak\Desktop\ScreenLog\app\lib --add-modules javafx.controls,javafx.fxml -Dinterceptor.mode=overlay -Dinterceptor.overlay=C:\Users\lucak\Downloads\FakeWindow.png -Dinterceptor.overlay.x=1200 -Dinterceptor.overlay.y=200 -jar C:\Users\lucak\Desktop\ScreenLog\app\screenlog-1.0.0.jar
pause