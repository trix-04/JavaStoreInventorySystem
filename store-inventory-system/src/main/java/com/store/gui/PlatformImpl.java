package com.store.gui;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to access JavaFX platform implementation details
 * Used for proper JavaFX shutdown and transition to console mode
 */
public class PlatformImpl {
    private static final List<FinishListener> finishListeners = new ArrayList<>();
    
    /**
     * Interface for monitoring JavaFX platform exit
     */
    public interface FinishListener {
        void idle(boolean isIdle);
        void exitedLastNestedLoop();
    }
    
    /**
     * Add a listener for JavaFX platform exit events
     */
    public static void addListener(FinishListener listener) {
        finishListeners.add(listener);
        
        // Add an actual listener to JavaFX platform via reflection
        try {
            Class<?> platformImplClass = Class.forName("com.sun.javafx.application.PlatformImpl");
            Method addListener = platformImplClass.getDeclaredMethod("addListener", Object.class);
            addListener.setAccessible(true);
            
            Object internalListener = createInternalListener(listener);
            addListener.invoke(null, internalListener);
        } catch (Exception e) {
            System.err.println("Warning: Failed to add JavaFX platform listener: " + e);
        }
    }
    
    /**
     * Create a listener that matches the internal JavaFX API
     */
    private static Object createInternalListener(FinishListener listener) throws Exception {
        Class<?> listenerClass = Class.forName("com.sun.javafx.application.PlatformImpl$FinishListener");
        Object proxy = java.lang.reflect.Proxy.newProxyInstance(
            PlatformImpl.class.getClassLoader(),
            new Class[]{listenerClass},
            (proxy1, method, args) -> {
                if ("idle".equals(method.getName()) && args.length == 1) {
                    listener.idle((Boolean)args[0]);
                    return null;
                } else if ("exitedLastNestedLoop".equals(method.getName())) {
                    listener.exitedLastNestedLoop();
                    return null;
                }
                return null;
            }
        );
        return proxy;
    }
}
