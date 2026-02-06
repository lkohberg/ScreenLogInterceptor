package com.criminal.interceptor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ImageModifier - Image manipulation logic for the ScreenLog Interceptor
 * 
 * This class contains all the image manipulation logic that gets called by the injected code.
 */
public class ImageModifier {
    
    private static final AtomicInteger screenshotCounter = new AtomicInteger(0);
    private static final String BACKUP_DIR = "interceptor_originals";
    
    /**
     * Main entry point for image modification
     * Called by injected code in ScreenshotService
     */
    public static BufferedImage modify(BufferedImage original) {
        if (original == null) {
            return original;
        }
        
        // Increment counter
        int count = screenshotCounter.incrementAndGet();
        
        // Save original backup
        String backupFilename = saveOriginalBackup(original);
        
        // Create deep copy for modification
        BufferedImage modified = deepCopy(original);
        
        // HIER: Aus der System-Property (die zuvor in parseArguments gesetzt wurde)
        String mode = System.getProperty("interceptor.mode", "stealth");
        
        // Print interception header
        System.out.println("╔════════════════════════════════════════════════════╗");
        System.out.println("  SCREENSHOT INTERCEPTED #" + count);
        System.out.println("╠════════════════════════════════════════════════════╣");
        System.out.println("  Mode: " + mode.toUpperCase()); // Zeigt: OVERLAY, REDACT, TIMESTAMP oder STEALTH
        System.out.println("  Original saved: " + backupFilename);
        
        // Anwendung des Modus
        switch (mode.toLowerCase()) {
            case "overlay":
                modified = applyOverlay(modified);
                break;
            case "redact":
                modified = redactRegions(modified);
                break;
            case "timestamp":
                modified = falsifyTimestamp(modified);
                break;
            case "stealth":
            default:
                modified = stealthModification(modified);
                break;
        }
        
        System.out.println("  Status: ✓ MODIFICATION COMPLETE");
        System.out.println("╚════════════════════════════════════════════════════╝");
        System.out.println();
        
        return modified;
    }
    
    /**
     * Save original screenshot to backup directory
     */
    private static String saveOriginalBackup(BufferedImage original) {
        try {
            // Create backup directory if it doesn't exist
            File backupDir = new File(BACKUP_DIR);
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }
            
            // Generate filename with timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
            String timestamp = sdf.format(new Date());
            String filename = "original_" + timestamp + ".png";
            
            // Save the image
            File outputFile = new File(backupDir, filename);
            ImageIO.write(original, "PNG", outputFile);
            
            return filename;
        } catch (IOException e) {
            System.err.println("ERROR: Failed to save original backup: " + e.getMessage());
            return "FAILED";
        }
    }
    
    /**
     * Create a deep copy of a BufferedImage
     */
    private static BufferedImage deepCopy(BufferedImage original) {
        BufferedImage copy = new BufferedImage(
            original.getWidth(),
            original.getHeight(),
            original.getType()
        );
        
        Graphics2D g = copy.createGraphics();
        g.drawImage(original, 0, 0, null);
        g.dispose();
        
        return copy;
    }
    
    /**
     * Apply stealth modification - modify single pixel invisibly
     */
    private static BufferedImage stealthModification(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        // Modify bottom-right pixel
        int x = width - 1;
        int y = height - 1;
        
        int originalRGB = image.getRGB(x, y);
        int modifiedRGB = originalRGB ^ 0x00000001; // XOR with 1 to flip least significant bit
        
        image.setRGB(x, y, modifiedRGB);
        
        System.out.println("  Action: Stealth modification (1 pixel)");
        System.out.println("  Location: (" + x + ", " + y + ")");
        System.out.println("  Original RGB:  0x" + Integer.toHexString(originalRGB));
        System.out.println("  Modified RGB:  0x" + Integer.toHexString(modifiedRGB));
        System.out.println("  Visual impact: None (invisible)");
        System.out.println("  Hash impact:   Complete (totally different)");
        
        return image;
    }
    
    /**
     * Apply overlay image on top of screenshot
     */
    private static BufferedImage applyOverlay(BufferedImage image) {
        // Check if overlay should only be applied when ChatGPT window is open
        boolean chatGPTOnly = Boolean.parseBoolean(
            System.getProperty("interceptor.overlay.chatgpt-only", "false")
        );
        
        if (chatGPTOnly) {
            boolean isChatGPTOpen = WindowDetector.isChatGPTWindowOpen();
            
            if (!isChatGPTOpen) {
                System.out.println("  Action: Overlay (skipped - ChatGPT window not detected)");
                System.out.println("  Visual impact: None");
                System.out.println("  Hash impact:   None");
                return image;
            }
            
            System.out.println("  ChatGPT window detected: YES");
        }
        
        String overlayPath = System.getProperty("interceptor.overlay");
        
        if (overlayPath == null || overlayPath.trim().isEmpty()) {
            System.out.println("  Action: Overlay (no overlay path specified)");
            System.out.println("  Visual impact: None");
            System.out.println("  Hash impact:   None");
            return image;
        }
        
        try {
            int x = Integer.parseInt(System.getProperty("interceptor.overlay.x", "0"));
            int y = Integer.parseInt(System.getProperty("interceptor.overlay.y", "0"));
            
            File overlayFile = new File(overlayPath);
            BufferedImage overlay = ImageIO.read(overlayFile);
            
            Graphics2D g = image.createGraphics();
            g.drawImage(overlay, x, y, null);
            g.dispose();
            
            System.out.println("  Action: Overlay image applied");
            System.out.println("  Overlay file: " + overlayPath);
            System.out.println("  Position: (" + x + ", " + y + ")");
            System.out.println("  Size: " + overlay.getWidth() + "x" + overlay.getHeight());
            System.out.println("  Visual impact: Visible overlay");
            System.out.println("  Hash impact:   Complete (totally different)");
            
        } catch (IOException | NumberFormatException e) {
            System.err.println("  ERROR: Failed to apply overlay: " + e.getMessage());
            System.out.println("  Visual impact: None (error)");
            System.out.println("  Hash impact:   None (error)");
        }
        
        return image;
    }
    
    /**
     * Redact specified regions by blacking them out
     */
    private static BufferedImage redactRegions(BufferedImage image) {
        String regionsStr = System.getProperty("interceptor.redact.regions");
        
        if (regionsStr == null || regionsStr.trim().isEmpty()) {
            System.out.println("  Action: Redact (no regions specified)");
            System.out.println("  Visual impact: None");
            System.out.println("  Hash impact:   None");
            return image;
        }
        
        Graphics2D g = image.createGraphics();
        g.setColor(Color.BLACK);
        
        String[] regions = regionsStr.split(";");
        int redactedCount = 0;
        
        System.out.println("  Action: Redacting " + regions.length + " region(s)");
        
        for (String region : regions) {
            String[] coords = region.trim().split(",");
            if (coords.length != 4) {
                System.err.println("  WARNING: Invalid region format (expected x,y,w,h): " + region);
                continue;
            }
            
            try {
                int x = Integer.parseInt(coords[0].trim());
                int y = Integer.parseInt(coords[1].trim());
                int w = Integer.parseInt(coords[2].trim());
                int h = Integer.parseInt(coords[3].trim());
                
                g.fillRect(x, y, w, h);
                System.out.println("  → Region " + (redactedCount + 1) + ": (" + x + ", " + y + ", " + w + "x" + h + ")");
                redactedCount++;
            } catch (NumberFormatException e) {
                System.err.println("  WARNING: Invalid coordinates in region (must be integers): " + region);
            }
        }
        
        g.dispose();
        
        System.out.println("  Redacted: " + redactedCount + " region(s)");
        System.out.println("  Visual impact: Black boxes visible");
        System.out.println("  Hash impact:   Complete (totally different)");
        
        return image;
    }
    
    /**
     * Draw fake timestamp on image
     */
    private static BufferedImage falsifyTimestamp(BufferedImage image) {
        String fakeTime = System.getProperty("interceptor.fake.time");
        
        if (fakeTime == null || fakeTime.trim().isEmpty()) {
            // Use a default fake time
            fakeTime = "2026-02-05 09:00:00";
        }
        
        Graphics2D g = image.createGraphics();
        
        // Set up font and colors
        g.setFont(new Font("Monospaced", Font.BOLD, 24));
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw background rectangle for better visibility
        String text = fakeTime;
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        
        int x = 10;
        int y = image.getHeight() - 30;
        
        // Draw semi-transparent black background
        g.setColor(new Color(255, 255, 255, 180));
        g.fillRect(x - 5, y - textHeight + 5, textWidth + 10, textHeight + 5);
        
        // Draw white text
        g.setColor(Color.WHITE);
        g.drawString(text, x, y);
        
        g.dispose();
        
        System.out.println("  Action: Fake timestamp drawn");
        System.out.println("  Fake time: " + fakeTime);
        System.out.println("  Position: Bottom-left corner");
        System.out.println("  Visual impact: Timestamp visible");
        System.out.println("  Hash impact:   Complete (totally different)");
        
        return image;
    }
}
