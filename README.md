# ScreenLog Interceptor

A Java agent that intercepts ScreenLog screenshot captures **BEFORE** they are hashed and added to the blockchain, allowing modification of screenshots while maintaining valid blockchain integrity.

## ⚠️ Legal Disclaimer

This software is provided for **educational and security research purposes only**. It demonstrates a fundamental vulnerability in systems that trust the data capture process itself. 

**Do not use this tool for:**
- Tampering with evidence in legal proceedings
- Fraudulent activities
- Violating workplace policies or academic integrity
- Any illegal or unethical purposes

The authors are not responsible for any misuse of this software. Use at your own risk and only in controlled testing environments where you have explicit permission.

## 🎯 Project Overview

ScreenLog is a blockchain-based screenshot logging system designed to create tamper-proof records of screen activity. However, it has a critical vulnerability: it trusts the screenshot capture process itself.

This interceptor exploits that vulnerability by:
1. Loading as a Java agent alongside ScreenLog
2. Using Javassist to intercept `ScreenshotService` methods at runtime
3. Modifying screenshots **BEFORE** they are hashed
4. Allowing the modified screenshots to be added to the blockchain
5. Maintaining valid blockchain integrity (all hashes check out!)

The result: A "tamper-proof" blockchain that contains tampered screenshots, with all cryptographic signatures validating correctly.

## 🔧 How the Attack Works

```
Normal ScreenLog Flow:
┌─────────────┐    ┌──────────┐    ┌────────────┐
│ Screenshot  │ -> │   Hash   │ -> │ Blockchain │
│   Capture   │    │ Calculate│    │   Storage  │
└─────────────┘    └──────────┘    └────────────┘

Interceptor Attack:
┌─────────────┐    ┌───────────┐    ┌──────────┐    ┌────────────┐
│ Screenshot  │ -> │ INTERCEPT │ -> │   Hash   │ -> │ Blockchain │
│   Capture   │    │  & MODIFY │    │ Calculate│    │   Storage  │
└─────────────┘    └───────────┘    └──────────┘    └────────────┘
```

The blockchain validates perfectly because it's hashing the **already-modified** screenshots!

## 📋 Requirements

- Java 17 or higher
- Maven 3.6 or higher
- ScreenLog application (target application)
- **Linux only**: `wmctrl` or `xdotool` (for ChatGPT window detection)

## 🚀 Installation

### Build from Source

```bash
# Clone the repository
git clone https://github.com/lkohberg/ScreenLogInterceptor.git
cd ScreenLogInterceptor

# Build with Maven
mvn clean package

# The JAR will be created at:
# target/screenlog-interceptor-1.0.0.jar
```

The build process:
- Compiles Java sources
- Packages the agent with all dependencies
- Creates a JAR with the proper manifest entries:
  - `Premain-Class: com.criminal.interceptor.ScreenLogInterceptor`
  - `Can-Redefine-Classes: true`
  - `Can-Retransform-Classes: true`

### Linux Setup (Optional)

For ChatGPT window detection on Linux, install one of these tools:

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

## 📖 Usage

The interceptor supports 4 modification modes:

### 1. Stealth Mode (Default)

Modifies a single pixel invisibly - changes the hash completely but is invisible to the human eye.

```bash
java -javaagent:target/screenlog-interceptor-1.0.0.jar \
     -Dinterceptor.mode=stealth \
     -jar screenlog-1.0.0.jar
```

**Expected Output:**
```
═══════════════════════════════════════════════════
  SCREENLOG INTERCEPTOR LOADED
═══════════════════════════════════════════════════
  Mode: stealth
  Status: READY TO INTERCEPT
═══════════════════════════════════════════════════

╔════════════════════════════════════════════════════╗
  SCREENSHOT INTERCEPTED #1
╠════════════════════════════════════════════════════╣
  Mode: STEALTH
  Action: Stealth modification (1 pixel)
  Location: (1919, 1079)
  Original RGB:  0xff1a1a1a
  Modified RGB:  0xff1a1a1b
  Visual impact: None (invisible)
  Hash impact:   Complete (totally different)
  Status: ✓ MODIFICATION COMPLETE
╚════════════════════════════════════════════════════╝
```

### 2. Overlay Mode

Places another image on top of the screenshot at specified coordinates.

#### Basic Overlay (Always Applied)

```bash
java -javaagent:target/screenlog-interceptor-1.0.0.jar \
     -Dinterceptor.mode=overlay \
     -Dinterceptor.overlay=/path/to/fake_window.png \
     -Dinterceptor.overlay.x=100 \
     -Dinterceptor.overlay.y=100 \
     -jar screenlog-1.0.0.jar
```

#### Conditional Overlay (ChatGPT Window Detection)

You can configure the overlay to only be applied when a ChatGPT window is currently open:

```bash
java -javaagent:target/screenlog-interceptor-1.0.0.jar \
     -Dinterceptor.mode=overlay \
     -Dinterceptor.overlay=/path/to/fake_window.png \
     -Dinterceptor.overlay.x=100 \
     -Dinterceptor.overlay.y=100 \
     -Dinterceptor.overlay.chatgpt-only=true \
     -jar screenlog-1.0.0.jar
```

**How ChatGPT Detection Works:**

When `chatgpt-only=true`, the interceptor will:
1. Check if any window with "ChatGPT" or "OpenAI" in the title is currently open
2. Only apply the overlay if such a window is detected
3. Skip the overlay if no ChatGPT window is found

**Platform Support:**
- **Windows**: Uses JNA to enumerate windows via Win32 API
- **macOS**: Uses AppleScript to query System Events
- **Linux**: Uses `wmctrl` or `xdotool` command-line tools (must be installed)

**Detection Behavior:**
- If detection fails (e.g., missing tools), defaults to "fail open" (allows overlay)
- Window must be visible (not minimized)
- Works across all major operating systems

**Use Cases:**
- Cover sensitive information with fake content only when needed
- Add fake windows or applications conditionally
- Replace real screen content selectively based on context


### 3. Redact Mode

Blacks out specified regions of the screenshot.

```bash
java -javaagent:target/screenlog-interceptor-1.0.0.jar \
     -Dinterceptor.mode=redact \
     -Dinterceptor.redact.regions="0,0,200,50;1720,0,200,50" \
     -jar screenlog-1.0.0.jar
```

**Region Format:** `x,y,width,height` separated by semicolons
- `0,0,200,50` - Top-left corner (200x50 pixels)
- `1720,0,200,50` - Top-right corner (200x50 pixels)

**Use Cases:**
- Hide taskbar/menu bar
- Redact specific UI elements
- Remove identifying information

### 4. Timestamp Mode

Draws a fake timestamp on the screenshot.

```bash
java -javaagent:target/screenlog-interceptor-1.0.0.jar \
     -Dinterceptor.mode=timestamp \
     -Dinterceptor.fake.time="2026-02-05 09:00:00" \
     -jar screenlog-1.0.0.jar
```

**Use Cases:**
- Falsify when screenshots were taken
- Create false alibis
- Manipulate temporal evidence

## 🔍 How It Works (Technical Details)

### 1. Agent Loading

When the JVM starts with `-javaagent:`, it calls the `premain()` method before the main application loads.

```java
public static void premain(String agentArgs, Instrumentation inst) {
    // Parse arguments
    // Register ClassFileTransformer
    // Print status banner
}
```

### 2. Bytecode Transformation

The agent registers a `ClassFileTransformer` that intercepts when `ScreenshotService` is loaded:

```java
class ScreenshotTransformer implements ClassFileTransformer {
    public byte[] transform(...) {
        // Use Javassist to modify bytecode
        // Find methods returning BufferedImage
        // Inject: method.insertAfter("{ $_ = ImageModifier.modify($_); }")
    }
}
```

### 3. Runtime Interception

When ScreenLog captures a screenshot:
1. Original method executes normally
2. Injected code intercepts the return value
3. `ImageModifier.modify()` is called
4. Modified screenshot is returned
5. ScreenLog hashes the **modified** screenshot
6. Modified screenshot + hash goes into blockchain

### 4. Window Detection (Overlay Mode)

When `chatgpt-only=true` is enabled:

**Windows:**
- Uses JNA (Java Native Access) to call Win32 API
- Enumerates all windows via `EnumWindows`
- Checks window titles for "ChatGPT" or "OpenAI"

**macOS:**
- Executes AppleScript via `osascript`
- Queries System Events for window titles
- Searches for "ChatGPT" or "OpenAI" in titles

**Linux:**
- Tries `wmctrl -l` first (lists all windows)
- Falls back to `xdotool search --name` if wmctrl unavailable
- Searches for "ChatGPT" or "OpenAI" in window titles

## 🧪 Testing Window Detection

Test the ChatGPT window detection without running ScreenLog:

```bash
mvn compile exec:java -Dexec.mainClass="com.criminal.interceptor.WindowDetectorTest"
```

This will show:
- Your operating system
- Whether a ChatGPT window is currently detected
- Instructions on how to test the feature

## 🐛 Troubleshooting

### Agent Not Loading

**Symptom:** No "SCREENLOG INTERCEPTOR LOADED" message

**Solutions:**
- Verify JAR path is correct
- Check Java version is 17+
- Ensure `-javaagent:` comes **before** `-jar`

### Class Not Transformed

**Symptom:** No "TRANSFORMING CLASS" message

**Solutions:**
- Verify ScreenLog is using the expected class name
- Check that ScreenLog is running in the same JVM
- Ensure Javassist dependency is included in the JAR

### Images Not Modified

**Symptom:** Screenshots look identical to originals

**Solutions:**
- Check mode is set correctly
- Verify system properties are passed correctly
- Check console output for errors
- For overlay mode, verify image file exists and path is correct

### ChatGPT Window Not Detected

**Solutions:**
- Ensure ChatGPT window is actually open and visible (not minimized)
- Check window title contains "ChatGPT" or "OpenAI"
- On Linux: Install `wmctrl` or `xdotool`
- Run the window detection test (see Testing section above)

### Build Failures

```bash
# Clean and rebuild
mvn clean package

# Run with debug output
mvn clean package -X

# Verify Java version
java -version  # Should be 17+
mvn -version   # Should be 3.6+
```

## 🏗️ Project Structure

```
ScreenLogInterceptor/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── criminal/
│                   └── interceptor/
│                       ├── ScreenLogInterceptor.java  # Main agent entry point
│                       ├── ImageModifier.java         # Image manipulation logic
│                       └── WindowDetector.java        # Window detection utility
├── pom.xml                         # Maven build configuration
├── .gitignore                      # Excludes build artifacts
└── README.md                       # This file
```

## 📝 System Properties Reference

| Property | Mode | Description | Example |
|----------|------|-------------|---------|
| `interceptor.mode` | All | Modification mode | `stealth`, `overlay`, `redact`, `timestamp` |
| `interceptor.overlay` | Overlay | Path to overlay image | `/path/to/image.png` |
| `interceptor.overlay.x` | Overlay | X coordinate for overlay | `100` |
| `interceptor.overlay.y` | Overlay | Y coordinate for overlay | `100` |
| `interceptor.overlay.chatgpt-only` | Overlay | Only overlay when ChatGPT window is open | `true`, `false` (default) |
| `interceptor.redact.regions` | Redact | Regions to black out | `x,y,w,h;x,y,w,h` |
| `interceptor.fake.time` | Timestamp | Fake timestamp text | `2026-02-05 09:00:00` |


## 📄 License

This project is provided for educational purposes. Use responsibly and legally.

---

**Remember:** This tool demonstrates a vulnerability in blockchain-based logging systems. The lesson is that **trust in data capture processes** is a critical security assumption that must be protected with hardware-level security, not just cryptography at the application layer.
