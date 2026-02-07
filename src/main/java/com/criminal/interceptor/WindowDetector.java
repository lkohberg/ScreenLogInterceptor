package com.criminal.interceptor;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.Pointer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * WindowDetector - Cross-platform window detection utility
 * <p>
 * Detects if specific windows (like ChatGPT) are currently open.
 */
public class WindowDetector {

    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    private static final boolean IS_WINDOWS = OS_NAME.contains("win");
    private static final boolean IS_MAC = OS_NAME.contains("mac");
    private static final boolean IS_LINUX = OS_NAME.contains("linux");

    // Extended User32 interface with IsIconic method
    public interface ExtendedUser32 extends User32 {
        ExtendedUser32 INSTANCE = Native.load("user32", ExtendedUser32.class);
        boolean IsIconic(WinDef.HWND hWnd);
    }

    /**
     * Check if any AI assistant window is currently open AND visible
     *
     * @return true if any AI assistant window is detected and visible, false otherwise
     */
    public static boolean isAIWindowOpen() {
        try {
            List<String> detectedAIs = getDetectedAIAssistants();
            return !detectedAIs.isEmpty();
        } catch (Exception e) {
            System.err.println("WARNING: Failed to detect AI assistant window: " + e.getMessage());
            // If detection fails, default to allowing overlay (fail open)
            return true;
        }
    }

    /**
     * Get list of detected AI assistants with their window titles
     *
     * @return List of detected AI assistant names
     */
    public static List<String> getDetectedAIAssistants() {
        List<String> detectedAIs = new ArrayList<>();

        try {
            List<WindowInfo> visibleWindows = getVisibleWindowTitles();

            // Check for AI assistant-related window titles
            for (WindowInfo windowInfo : visibleWindows) {
                String lowerTitle = windowInfo.title.toLowerCase();

                // ChatGPT / OpenAI
                if (lowerTitle.contains("chatgpt") ||
                        lowerTitle.contains("chat gpt") ||
                        lowerTitle.contains("openai") ||
                        (lowerTitle.contains("chat") && lowerTitle.contains("openai"))) {
                    detectedAIs.add("ChatGPT (" + windowInfo.title + ")");
                    continue;
                }

                // Google Gemini / Bard
                if (lowerTitle.contains("gemini") ||
                        lowerTitle.contains("bard") ||
                        lowerTitle.contains("google ai")) {
                    detectedAIs.add("Gemini (" + windowInfo.title + ")");
                    continue;
                }

                // Anthropic Claude
                if (lowerTitle.contains("claude") ||
                        lowerTitle.contains("anthropic")) {
                    detectedAIs.add("Claude (" + windowInfo.title + ")");
                    continue;
                }

                // Microsoft Copilot / GitHub Copilot
                if (lowerTitle.contains("copilot") ||
                        lowerTitle.contains("github copilot") ||
                        lowerTitle.contains("microsoft copilot")) {
                    detectedAIs.add("Copilot (" + windowInfo.title + ")");
                    continue;
                }
            }
        } catch (Exception e) {
            System.err.println("WARNING: Failed to detect AI assistant windows: " + e.getMessage());
        }

        return detectedAIs;
    }

    /**
     * Check if a ChatGPT window is currently open
     * @deprecated Use isAIWindowOpen() for broader AI detection
     */
    @Deprecated
    public static boolean isChatGPTWindowOpen() {
        return isAIWindowOpen();
    }

    /**
     * Window information container
     */
    private static class WindowInfo {
        String title;
        boolean isVisible;
        boolean isMinimized;

        WindowInfo(String title, boolean isVisible, boolean isMinimized) {
            this.title = title;
            this.isVisible = isVisible;
            this.isMinimized = isMinimized;
        }
    }

    /**
     * Get all visible (non-minimized) window titles
     *
     * @return List of visible window information
     */
    private static List<WindowInfo> getVisibleWindowTitles() {
        if (IS_WINDOWS) {
            return getVisibleWindowTitlesWindows();
        } else if (IS_MAC) {
            return getVisibleWindowTitlesMac();
        } else if (IS_LINUX) {
            return getVisibleWindowTitlesLinux();
        } else {
            System.err.println("WARNING: Unsupported OS for window detection: " + OS_NAME);
            return new ArrayList<>();
        }
    }

    /**
     * Get visible window titles on Windows using JNA with minimization check
     */
    private static List<WindowInfo> getVisibleWindowTitlesWindows() {
        List<WindowInfo> windows = new ArrayList<>();

        try {
            User32 user32 = User32.INSTANCE;
            ExtendedUser32 extUser32 = ExtendedUser32.INSTANCE;

            user32.EnumWindows(new WinUser.WNDENUMPROC() {
                @Override
                public boolean callback(WinDef.HWND hWnd, Pointer data) {
                    // Check if window is visible and not minimized (using our extended interface)
                    if (user32.IsWindowVisible(hWnd) && !extUser32.IsIconic(hWnd)) {
                        char[] buffer = new char[1024];
                        user32.GetWindowText(hWnd, buffer, buffer.length);
                        String title = Native.toString(buffer);

                        if (title != null && !title.trim().isEmpty()) {
                            windows.add(new WindowInfo(title, true, false));
                        }
                    }
                    return true;
                }
            }, null);
        } catch (Exception e) {
            System.err.println("WARNING: Failed to enumerate Windows windows: " + e.getMessage());
            // Fallback: use basic visibility check only
            return getBasicVisibleWindowsWindows();
        }

        return windows;
    }

    /**
     * Fallback method for Windows without IsIconic check
     */
    private static List<WindowInfo> getBasicVisibleWindowsWindows() {
        List<WindowInfo> windows = new ArrayList<>();

        try {
            User32 user32 = User32.INSTANCE;

            user32.EnumWindows(new WinUser.WNDENUMPROC() {
                @Override
                public boolean callback(WinDef.HWND hWnd, Pointer data) {
                    // Only check if window is visible (no minimization check)
                    if (user32.IsWindowVisible(hWnd)) {
                        char[] buffer = new char[1024];
                        user32.GetWindowText(hWnd, buffer, buffer.length);
                        String title = Native.toString(buffer);

                        if (title != null && !title.trim().isEmpty()) {
                            windows.add(new WindowInfo(title, true, false));
                        }
                    }
                    return true;
                }
            }, null);
        } catch (Exception e) {
            System.err.println("WARNING: Failed to enumerate Windows windows (fallback): " + e.getMessage());
        }

        return windows;
    }

    /**
     * Get visible window titles on macOS using AppleScript with visibility check
     */
    private static List<WindowInfo> getVisibleWindowTitlesMac() {
        List<WindowInfo> windows = new ArrayList<>();

        try {
            // Use AppleScript to get only visible (non-minimized) window titles
            ProcessBuilder pb = new ProcessBuilder(
                    "osascript",
                    "-e",
                    "tell application \"System Events\" to get name of every window of every process whose visible is true and miniaturized is false"
            );
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                // AppleScript returns comma-separated list
                String[] windowTitles = line.split(", ");
                for (String title : windowTitles) {
                    if (title != null && !title.trim().isEmpty()) {
                        windows.add(new WindowInfo(title.trim(), true, false));
                    }
                }
            }

            process.waitFor();
        } catch (Exception e) {
            System.err.println("WARNING: Failed to get macOS window titles: " + e.getMessage());
        }

        return windows;
    }

    /**
     * Get visible window titles on Linux using wmctrl or xdotool with visibility check
     */
    private static List<WindowInfo> getVisibleWindowTitlesLinux() {
        List<WindowInfo> windows = new ArrayList<>();

        // Try wmctrl first (more efficient)
        if (tryLinuxCommandWmctrlVisible(windows)) {
            return windows;
        }

        // Try xdotool as fallback (batch mode for efficiency)
        if (tryLinuxCommandXdotoolVisible(windows)) {
            return windows;
        }

        System.err.println("WARNING: Neither wmctrl nor xdotool found on Linux system");
        return windows;
    }

    /**
     * Try wmctrl command to get visible (non-minimized) window titles
     */
    private static boolean tryLinuxCommandWmctrlVisible(List<WindowInfo> windows) {
        try {
            ProcessBuilder pb = new ProcessBuilder("wmctrl", "-l");
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                // wmctrl format: "0x... desktop hostname title"
                String[] parts = line.split("\\s+", 4);
                if (parts.length >= 4) {
                    String windowId = parts[0];
                    String title = parts[3];

                    // Check if window is minimized using xprop
                    if (!isWindowMinimizedLinux(windowId)) {
                        windows.add(new WindowInfo(title, true, false));
                    }
                }
            }

            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if a Linux window is minimized using xprop
     */
    private static boolean isWindowMinimizedLinux(String windowId) {
        try {
            ProcessBuilder pb = new ProcessBuilder("xprop", "-id", windowId, "_NET_WM_STATE");
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().contains("hidden") || line.toLowerCase().contains("minimized")) {
                    return true;
                }
            }

            process.waitFor();
            return false;
        } catch (Exception e) {
            return false; // Assume not minimized if we can't check
        }
    }

    /**
     * Try xdotool command to get visible window titles (optimized batch mode)
     */
    private static boolean tryLinuxCommandXdotoolVisible(List<WindowInfo> windows) {
        try {
            // Use xdotool to search for visible windows only
            ProcessBuilder pb = new ProcessBuilder(
                    "bash", "-c",
                    "xdotool search --onlyvisible --name '.*' 2>/dev/null | while read id; do " +
                    "if ! xprop -id $id _NET_WM_STATE 2>/dev/null | grep -q HIDDEN; then " +
                    "xdotool getwindowname $id 2>/dev/null; fi; done"
            );
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    windows.add(new WindowInfo(line.trim(), true, false));
                }
            }

            // Consume error stream to prevent blocking
            while (errorReader.readLine() != null) {
                // Ignore errors
            }

            int exitCode = process.waitFor();
            return exitCode == 0 && !windows.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    // Keep old methods for compatibility
    private static List<String> getWindowTitlesWindows() {
        List<WindowInfo> windowInfos = getVisibleWindowTitlesWindows();
        List<String> titles = new ArrayList<>();
        for (WindowInfo info : windowInfos) {
            titles.add(info.title);
        }
        return titles;
    }

    private static List<String> getWindowTitlesMac() {
        List<WindowInfo> windowInfos = getVisibleWindowTitlesMac();
        List<String> titles = new ArrayList<>();
        for (WindowInfo info : windowInfos) {
            titles.add(info.title);
        }
        return titles;
    }

    private static List<String> getWindowTitlesLinux() {
        List<WindowInfo> windowInfos = getVisibleWindowTitlesLinux();
        List<String> titles = new ArrayList<>();
        for (WindowInfo info : windowInfos) {
            titles.add(info.title);
        }
        return titles;
    }
}
