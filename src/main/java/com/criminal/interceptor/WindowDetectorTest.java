package com.criminal.interceptor;

import java.util.List;

/**
 * Simple test program to verify WindowDetector functionality
 */
public class WindowDetectorTest {
    
    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("  AI Window Detector Test");
        System.out.println("===========================================");
        System.out.println();
        
        System.out.println("Operating System: " + System.getProperty("os.name"));
        System.out.println();
        
        System.out.println("Testing AI assistant window detection...");

        try {
            List<String> detectedAIs = WindowDetector.getDetectedAIAssistants();
            boolean isAIOpen = !detectedAIs.isEmpty();

            System.out.println("AI assistant window detected: " + (isAIOpen ? "YES" : "NO"));

            if (isAIOpen) {
                System.out.println("-- Found " + detectedAIs.size() + " AI assistant window(s):");
                for (int i = 0; i < detectedAIs.size(); i++) {
                    System.out.println("  " + (i + 1) + ". " + detectedAIs.get(i));
                }
            } else {
                System.out.println("!! No AI assistant window detected");
                System.out.println();
                System.out.println("To test this feature, open a browser with:");
                System.out.println("* ChatGPT (chat.openai.com)");
                System.out.println("* Google Gemini (gemini.google.com)");
                System.out.println("* Claude (claude.ai)");
                System.out.println("* GitHub Copilot or Microsoft Copilot");
                System.out.println("Make sure the AI name appears in the window title");
            }
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
        System.out.println("===========================================");
    }
}
