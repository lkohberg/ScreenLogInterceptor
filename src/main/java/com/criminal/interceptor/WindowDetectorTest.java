package com.criminal.interceptor;

/**
 * Simple test program to verify WindowDetector functionality
 */
public class WindowDetectorTest {
    
    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("  Window Detector Test");
        System.out.println("===========================================");
        System.out.println();
        
        System.out.println("Operating System: " + System.getProperty("os.name"));
        System.out.println();
        
        System.out.println("Testing ChatGPT window detection...");
        
        try {
            boolean isChatGPTOpen = WindowDetector.isChatGPTWindowOpen();
            
            System.out.println("ChatGPT window detected: " + (isChatGPTOpen ? "YES" : "NO"));
            
            if (isChatGPTOpen) {
                System.out.println("✓ A ChatGPT or OpenAI window is currently open");
            } else {
                System.out.println("✗ No ChatGPT or OpenAI window detected");
                System.out.println();
                System.out.println("To test this feature:");
                System.out.println("1. Open a browser with ChatGPT");
                System.out.println("2. Make sure 'ChatGPT' or 'OpenAI' appears in the window title");
                System.out.println("3. Run this test again");
            }
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
        System.out.println("===========================================");
    }
}
