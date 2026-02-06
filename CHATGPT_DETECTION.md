# ChatGPT Window Detection Feature

This feature allows the ScreenLog Interceptor to conditionally apply overlays only when a ChatGPT window is detected.

## How It Works

When enabled, the interceptor will:
1. Check if any window with "ChatGPT" or "OpenAI" in the title is currently open
2. Only apply the overlay if such a window is detected
3. Skip the overlay if no ChatGPT window is found

## Platform Support

- **Windows**: Uses JNA to enumerate windows via Win32 API
- **macOS**: Uses AppleScript to query System Events
- **Linux**: Uses `wmctrl` or `xdotool` command-line tools (must be installed)

## Usage

### Basic Overlay (Always Applied)

```bash
java -javaagent:target/screenlog-interceptor-1.0.0.jar \
     -Dinterceptor.mode=overlay \
     -Dinterceptor.overlay=/path/to/overlay.png \
     -Dinterceptor.overlay.x=100 \
     -Dinterceptor.overlay.y=100 \
     -jar screenlog-1.0.0.jar
```

### Conditional Overlay (Only When ChatGPT Window is Open)

```bash
java -javaagent:target/screenlog-interceptor-1.0.0.jar \
     -Dinterceptor.mode=overlay \
     -Dinterceptor.overlay=/path/to/overlay.png \
     -Dinterceptor.overlay.x=100 \
     -Dinterceptor.overlay.y=100 \
     -Dinterceptor.overlay.chatgpt-only=true \
     -jar screenlog-1.0.0.jar
```

## Configuration Options

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `interceptor.overlay.chatgpt-only` | boolean | `false` | Only apply overlay when ChatGPT window is detected |

## Testing

You can test the window detection without running ScreenLog:

```bash
mvn compile exec:java -Dexec.mainClass="com.criminal.interceptor.WindowDetectorTest"
```

This will show:
- Your operating system
- Whether a ChatGPT window is currently detected
- Instructions on how to test the feature

## Linux Requirements

On Linux, you need either `wmctrl` or `xdotool` installed:

**Ubuntu/Debian:**
```bash
sudo apt-get install wmctrl
# or
sudo apt-get install xdotool
```

**Fedora/RHEL:**
```bash
sudo dnf install wmctrl
# or
sudo dnf install xdotool
```

**Arch Linux:**
```bash
sudo pacman -S wmctrl
# or
sudo pacman -S xdotool
```

## Troubleshooting

### Window Not Detected

Make sure:
1. The ChatGPT window is actually open
2. The window title contains "ChatGPT", "OpenAI", or similar text
3. On Linux, `wmctrl` or `xdotool` is installed
4. The window is visible (not minimized)

### Detection Fails Open

If window detection fails (e.g., due to missing tools on Linux), the system will default to **allowing** the overlay. This is a "fail open" design to ensure functionality even when detection is unavailable.

## Example Output

When ChatGPT window is detected:
```
╔════════════════════════════════════════════════════╗
  SCREENSHOT INTERCEPTED #1
╠════════════════════════════════════════════════════╣
  Mode: OVERLAY
  Original saved: original_2026-02-06_14-23-15-456.png
  ChatGPT window detected: YES
  Action: Overlay image applied
  Overlay file: /path/to/overlay.png
  Position: (100, 100)
  Size: 800x600
  Visual impact: Visible overlay
  Hash impact:   Complete (totally different)
  Status: ✓ MODIFICATION COMPLETE
╚════════════════════════════════════════════════════╝
```

When ChatGPT window is NOT detected:
```
╔════════════════════════════════════════════════════╗
  SCREENSHOT INTERCEPTED #1
╠════════════════════════════════════════════════════╣
  Mode: OVERLAY
  Original saved: original_2026-02-06_14-23-15-456.png
  Action: Overlay (skipped - ChatGPT window not detected)
  Visual impact: None
  Hash impact:   None
  Status: ✓ MODIFICATION COMPLETE
╚════════════════════════════════════════════════════╝
```
