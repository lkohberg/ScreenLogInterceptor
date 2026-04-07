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

ScreenLog is a blockchain-based screenshot logging system designed to create tamper-proof records of screen activity. However, it has a critical vulnerability: it trusts the screenshot capture pro[...]

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
- **Linux only**: `wmctrl` or `xdotool` (for AI assistant window detection)

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

For AI assistant window detection on Linux, install one of these tools:

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

The interceptor supports Overlay mode:

### Overlay Mode

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

#### Conditional Overlay (AI Assistant Window Detection)

You can configure the overlay to only be applied when an AI assistant window is currently open:

```bash
java -javaagent:target/screenlog-interceptor-1.0.0.jar \
     -Dinterceptor.mode=overlay \
     -Dinterceptor.overlay=/path/to/fake_window.png \
     -Dinterceptor.overlay.x=100 \
     -Dinterceptor.overlay.y=100 \
     -Dinterceptor.overlay.ai-detection=true \
     -jar screenlog-1.0.0.jar
```

**Note:** The property `interceptor.overlay.chatgpt-only` is still supported for backward compatibility but `interceptor.overlay.ai-detection` is now the recommended property name.

**How AI Assistant Detection Works:**

When `ai-detection=true`, the interceptor will:
1. Check if any window with AI assistant keywords is currently open and visible
2. Only apply the overlay if such a window is detected
3. Skip the overlay if no AI assistant window is found

**Detected AI Assistants:**
- **ChatGPT / OpenAI**: Windows containing "ChatGPT", "Chat GPT", "OpenAI", or "Chat" + "OpenAI"
- **Google Gemini / Bard**: Windows containing "Gemini", "Bard", or "Google AI"
- **Anthropic Claude**: Windows containing "Claude" or "Anthropic"
- **Microsoft Copilot / GitHub Copilot**: Windows containing "Copilot", "GitHub Copilot", or "Microsoft Copilot"

**Platform Support:**
- **Windows**: Uses JNA to enumerate windows via Win32 API with minimization detection
- **macOS**: Uses AppleScript to query System Events
- **Linux**: Uses `wmctrl` or `xdotool` command-line tools (must be installed)

**Detection Behavior:**
- If detection fails (e.g., missing tools), defaults to "fail open" (allows overlay)
- Window must be visible and not minimized
- Detects multiple AI assistant types simultaneously
- Shows which AI assistants were detected in the console output
- Works across all major operating systems

**Use Cases:**
- Cover sensitive AI assistant usage when any AI tool is open
- Add fake windows or applications conditionally
- Replace real screen content selectively based on context

**Advanced Feature: Clean Screenshot Caching**

When AI detection is enabled (`ai-detection=true`), the interceptor implements intelligent screenshot caching:

1. **When NO AI assistant is detected**: The interceptor stores the screenshot as a "clean" screenshot cache and skips the overlay
2. **When an AI assistant IS detected**: 
   - If a clean screenshot cache exists, it uses that cached screenshot as the overlay (instead of the file)
   - If no cache exists, it falls back to the overlay file (if specified)
   - If neither exists, no overlay is applied

This creates a seamless effect where the screen appears unchanged even when an AI assistant is opened, by overlaying the previous clean state over the current AI-visible state.

**Expected Output (When AI Assistant Detected):**
```
=============================================
  SCREENSHOT INTERCEPTED #1
=============================================
  Mode: OVERLAY
  AI assistant window(s) detected: 2
  → 1. ChatGPT (ChatGPT - Google Chrome)
  → 2. Copilot (GitHub Copilot Chat - Visual Studio Code)
  Action: Overlay image applied (from file)
  Overlay file: /path/to/fake_window.png
  Position: (100, 100)
  Size: 800x600
  Visual impact: Visible overlay
  Hash impact:   Complete (totally different)
  Status: ✓ MODIFICATION COMPLETE
=============================================
```

**Expected Output (No AI Assistant Detected):**
```
=============================================
  SCREENSHOT INTERCEPTED #1
=============================================
  Mode: OVERLAY
  Action: Overlay (skipped - No AI assistant window detected)
  Checked: ChatGPT, Gemini, Claude, Copilot
  Visual impact: None
  Hash impact:   None
  → Stored as last clean screenshot
  Status: ✓ MODIFICATION COMPLETE
=============================================
```

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

When `ai-detection=true` is enabled:

**Windows:**
- Uses JNA (Java Native Access) to call Win32 API
- Enumerates all windows via `EnumWindows`
- Checks for minimized state using `IsIconic`
- Searches window titles for AI assistant keywords

**macOS:**
- Executes AppleScript via `osascript`
- Queries System Events for window titles
- Searches for AI assistant keywords in titles

**Linux:**
- Tries `wmctrl -l` first (lists all windows)
- Falls back to `xdotool search --name` if wmctrl unavailable
- Searches for AI assistant keywords in window titles

**Supported AI Assistants:**
- ChatGPT / OpenAI
- Google Gemini / Bard  
- Anthropic Claude
- Microsoft Copilot / GitHub Copilot

## 🧪 Testing Window Detection

Test the AI assistant window detection without running ScreenLog:

```bash
mvn compile exec:java -Dexec.mainClass="com.criminal.interceptor.WindowDetectorTest"
```

This will show:
- Your operating system
- Whether any AI assistant window is currently detected
- Which specific AI assistants are detected (ChatGPT, Gemini, Claude, Copilot)
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
- Ensure AI assistant window is actually open and visible (not minimized)
- Check window title contains one of the supported AI assistant keywords:
  - ChatGPT, OpenAI, Gemini, Bard, Claude, Anthropic, Copilot
- On Linux: Install `wmctrl` or `xdotool`
- Run the window detection test (see Testing section above)
- Note: Property name changed to `ai-detection` (old name `chatgpt-only` still works)

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
│                       ├── ScreenLogInterceptor.java     # Main agent entry point
│                       ├── ImageModifier.java            # Image manipulation logic
│                       ├── WindowDetector.java           # Window detection utility
│                       └── WindowDetectorTest.java       # Window detection test tool
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
| `interceptor.overlay.ai-detection` | Overlay | Only overlay when AI assistant window is open | `true`, `false` (default) |
| `interceptor.overlay.chatgpt-only` | Overlay | (Deprecated) Use `ai-detection` instead | `true`, `false` (default) |
| `interceptor.redact.regions` | Redact | Regions to black out | `x,y,w,h;x,y,w,h` |
| `interceptor.fake.time` | Timestamp | Fake timestamp text | `2026-02-05 09:00:00` |


## 📄 License

This project is provided for educational purposes. Use responsibly and legally.

---

**Remember:** This tool demonstrates a vulnerability in blockchain-based logging systems. The lesson is that **trust in data capture processes** is a critical security assumption that must be pro[...]
