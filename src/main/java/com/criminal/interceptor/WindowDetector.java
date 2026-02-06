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
 * 
 * Detects if specific windows (like ChatGPT) are currently open.
 */
public class WindowDetector {
    
    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    private static final boolean IS_WINDOWS = OS_NAME.contains("win");
    private static final boolean IS_MAC = OS_NAME.contains("mac");
    private static final boolean IS_LINUX = OS_NAME.contains("linux");
    
    /**
     * Check if a ChatGPT window is currently open
     * @return true if a ChatGPT window is detected, false otherwise
     */
    public static boolean isChatGPTWindowOpen() {
        try {
            List<String> windowTitles = getOpenWindowTitles();
            
            // Check for ChatGPT-related window titles
            for (String title : windowTitles) {
                String lowerTitle = title.toLowerCase();
                
                // Check for various ChatGPT window patterns
                if (lowerTitle.contains("chatgpt") || 
                    lowerTitle.contains("chat gpt") ||
                    lowerTitle.contains("openai") ||
                    (lowerTitle.contains("chat") && lowerTitle.contains("openai"))) {
                    return true;
                }
            }
            
            return false;
        } catch (Exception e) {
            System.err.println("WARNING: Failed to detect ChatGPT window: " + e.getMessage());
            // If detection fails, default to allowing overlay (fail open)
            return true;
        }
    }
    
    /**
     * Get all open window titles
     * @return List of window titles
     */
    private static List<String> getOpenWindowTitles() {
        if (IS_WINDOWS) {
            return getWindowTitlesWindows();
        } else if (IS_MAC) {
            return getWindowTitlesMac();
        } else if (IS_LINUX) {
            return getWindowTitlesLinux();
        } else {
            System.err.println("WARNING: Unsupported OS for window detection: " + OS_NAME);
            return new ArrayList<>();
        }
    }
    
    /**
     * Get window titles on Windows using JNA
     */
    private static List<String> getWindowTitlesWindows() {
        List<String> titles = new ArrayList<>();
        
        try {
            User32 user32 = User32.INSTANCE;
            
            user32.EnumWindows(new WinUser.WNDENUMPROC() {
                @Override
                public boolean callback(WinDef.HWND hWnd, Pointer data) {
                    if (user32.IsWindowVisible(hWnd)) {
                        char[] buffer = new char[1024];
                        user32.GetWindowText(hWnd, buffer, buffer.length);
                        String title = Native.toString(buffer);
                        
                        if (title != null && !title.trim().isEmpty()) {
                            titles.add(title);
                        }
                    }
                    return true;
                }
            }, null);
        } catch (Exception e) {
            System.err.println("WARNING: Failed to enumerate Windows windows: " + e.getMessage());
        }
        
        return titles;
    }
    
    /**
     * Get window titles on macOS using AppleScript
     */
    private static List<String> getWindowTitlesMac() {
        List<String> titles = new ArrayList<>();
        
        try {
            // Use AppleScript to get window titles
            String[] cmd = {
                "osascript",
                "-e",
                "tell application \"System Events\" to get name of every window of every process"
            };
            
            Process process = Runtime.getRuntime().exec(cmd);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String line;
            while ((line = reader.readLine()) != null) {
                // AppleScript returns comma-separated list
                String[] windowTitles = line.split(", ");
                for (String title : windowTitles) {
                    if (title != null && !title.trim().isEmpty()) {
                        titles.add(title.trim());
                    }
                }
            }
            
            process.waitFor();
        } catch (Exception e) {
            System.err.println("WARNING: Failed to get macOS window titles: " + e.getMessage());
        }
        
        return titles;
    }
    
    /**
     * Get window titles on Linux using wmctrl or xdotool
     */
    private static List<String> getWindowTitlesLinux() {
        List<String> titles = new ArrayList<>();
        
        // Try wmctrl first
        if (tryLinuxCommand(new String[]{"wmctrl", "-l"}, titles)) {
            return titles;
        }
        
        // Try xdotool as fallback
        if (tryLinuxCommand(new String[]{"xdotool", "search", "--onlyvisible", "--name", ".*"}, titles)) {
            return titles;
        }
        
        System.err.println("WARNING: Neither wmctrl nor xdotool found on Linux system");
        return titles;
    }
    
    /**
     * Try to execute a Linux command to get window titles
     */
    private static boolean tryLinuxCommand(String[] cmd, List<String> titles) {
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String line;
            while ((line = reader.readLine()) != null) {
                // wmctrl format: "0x... desktop ... title"
                // xdotool returns window IDs, need to get titles separately
                if (cmd[0].equals("wmctrl")) {
                    String[] parts = line.split("\\s+", 4);
                    if (parts.length >= 4) {
                        titles.add(parts[3]);
                    }
                } else if (cmd[0].equals("xdotool")) {
                    // Get window title for each window ID
                    String windowId = line.trim();
                    try {
                        Process titleProcess = Runtime.getRuntime().exec(
                            new String[]{"xdotool", "getwindowname", windowId}
                        );
                        BufferedReader titleReader = new BufferedReader(
                            new InputStreamReader(titleProcess.getInputStream())
                        );
                        String title = titleReader.readLine();
                        if (title != null && !title.trim().isEmpty()) {
                            titles.add(title);
                        }
                        titleProcess.waitFor();
                    } catch (Exception e) {
                        // Skip this window
                    }
                }
            }
            
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
