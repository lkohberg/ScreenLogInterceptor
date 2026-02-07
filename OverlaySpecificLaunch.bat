@echo off
echo ===================================================
echo   LAUNCHING SCREENLOG WITH INTERCEPTOR
echo ===================================================
echo.
echo Mode: OVERLAY (AI Detection: ChatGPT, Gemini, Claude, Copilot)
echo Status: Loading...
echo.

cd /d C:\Users\lucak\Desktop\ScreenLog

start "" java -javaagent:C:\JavaProjects\ScreenLogInterceptor\target\screenlog-interceptor-1.0.0.jar=mode=overlay,overlay=C:\JavaProjects\ScreenLogInterceptor\src\main\resources\FakeWindow.png,overlay.ai-detection=true,overlay.x=0,overlay.y=0 --module-path C:\Users\lucak\Desktop\ScreenLog\app\lib --add-modules javafx.controls,javafx.fxml -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -jar C:\Users\lucak\Desktop\ScreenLog\app\screenlog-1.0.0.jar

timeout /t 2 /nobreak
powershell -Command "Add-Type '[DllImport(\"user32.dll\")] public static extern void ShowWindow(int hwnd, int command);' -Name Win32 -Namespace Native -PassThru | % { $_.ShowWindow([diagnostics.process]::GetCurrentProcess().MainWindowHandle, 6) }"

exit
