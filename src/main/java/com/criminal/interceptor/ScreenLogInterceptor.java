package com.criminal.interceptor;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

/**
 * ScreenLog Interceptor Java Agent
 * 
 * This agent intercepts screenshot captures BEFORE they are hashed and added to the blockchain,
 * allowing modification of screenshots while maintaining valid blockchain integrity.
 */
public class ScreenLogInterceptor {
    
    private static String mode = "stealth";
    private static String overlayPath = null;
    private static int overlayX = 0;
    private static int overlayY = 0;
    private static String redactRegions = null;
    private static String fakeTime = null;
    
    /**
     * Java agent entry point
     * @param agentArgs Arguments passed to the agent (comma-separated key=value pairs)
     * @param inst Instrumentation instance
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        // Parse agent arguments
        parseArguments(agentArgs);
        
        // Print startup banner
        printStartupBanner();
        
        // Register the class transformer
        inst.addTransformer(new ScreenshotTransformer());
        
        System.out.println("  Transformer registered for: at/htl/screenlog/service/ScreenshotService");
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println();
    }
    
    /**
     * Parse agent arguments
     * Expected format: overlay=/path/to/image.png,mode=stealth
     */
    private static void parseArguments(String args) {
        if (args == null || args.trim().isEmpty()) {
            return;
        }
        
        String[] pairs = args.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length != 2) continue;
            
            String key = kv[0].trim();
            String value = kv[1].trim();
            
            switch (key) {
                case "mode":
                    mode = value;
                    System.setProperty("interceptor.mode", value);
                    break;
                case "overlay":
                    overlayPath = value;
                    System.setProperty("interceptor.overlay", value);
                    break;
                case "overlay.x":
                    overlayX = Integer.parseInt(value);
                    System.setProperty("interceptor.overlay.x", value);
                    break;
                case "overlay.y":
                    overlayY = Integer.parseInt(value);
                    System.setProperty("interceptor.overlay.y", value);
                    break;
                case "redact.regions":
                    redactRegions = value;
                    System.setProperty("interceptor.redact.regions", value);
                    break;
                case "fake.time":
                    fakeTime = value;
                    System.setProperty("interceptor.fake.time", value);
                    break;
            }
        }
    }
    
    /**
     * Print startup banner
     */
    private static void printStartupBanner() {
        System.out.println();
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("  SCREENLOG INTERCEPTOR LOADED");
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("  Mode: " + mode);
        
        if (mode.equals("overlay") && overlayPath != null) {
            System.out.println("  Overlay: " + overlayPath);
            System.out.println("  Position: (" + overlayX + ", " + overlayY + ")");
        } else if (mode.equals("redact") && redactRegions != null) {
            System.out.println("  Redact regions: " + redactRegions);
        } else if (mode.equals("timestamp") && fakeTime != null) {
            System.out.println("  Fake time: " + fakeTime);
        }
        
        System.out.println("  Status: READY TO INTERCEPT");
        System.out.println("═══════════════════════════════════════════════════");
    }
    
    /**
     * ClassFileTransformer that modifies ScreenshotService bytecode
     */
    static class ScreenshotTransformer implements ClassFileTransformer {
        
        @Override
        public byte[] transform(
                ClassLoader loader,
                String className,
                Class<?> classBeingRedefined,
                ProtectionDomain protectionDomain,
                byte[] classfileBuffer) {
            
            // Target ScreenshotService class
            if (!className.equals("at/htl/screenlog/service/ScreenshotService")) {
                return null; // Don't modify other classes
            }
            
            try {
                System.out.println("╔════════════════════════════════════════════════════╗");
                System.out.println("  TRANSFORMING CLASS: " + className);
                System.out.println("╚════════════════════════════════════════════════════╝");
                
                ClassPool pool = ClassPool.getDefault();
                CtClass ctClass = pool.get("at.htl.screenlog.service.ScreenshotService");
                
                // Find all methods that return BufferedImage
                CtMethod[] methods = ctClass.getDeclaredMethods();
                int modifiedCount = 0;
                
                for (CtMethod method : methods) {
                    String returnType = method.getReturnType().getName();
                    
                    if (returnType.equals("java.awt.image.BufferedImage")) {
                        System.out.println("  → Injecting into method: " + method.getName());
                        
                        // Inject our modification code AFTER the method returns
                        method.insertAfter(
                            "{ $_ = com.criminal.interceptor.ImageModifier.modify($_); }"
                        );
                        
                        modifiedCount++;
                    }
                }
                
                System.out.println("  ✓ Modified " + modifiedCount + " method(s)");
                System.out.println();
                
                byte[] bytecode = ctClass.toBytecode();
                ctClass.detach(); // Clean up
                return bytecode;
                
            } catch (Exception e) {
                System.err.println("ERROR transforming class: " + className);
                e.printStackTrace();
                return null;
            }
        }
    }
}
