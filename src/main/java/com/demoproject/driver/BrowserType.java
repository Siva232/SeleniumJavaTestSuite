package com.demoproject.driver;

import com.demoproject.exceptions.FrameworkException;

import java.util.Arrays;
import java.util.Locale;

/**
 * Supported browsers.
 */
public enum BrowserType {
    CHROME,
    FIREFOX,
    EDGE;

    /**
     * @param value configuration value (case-insensitive)
     * @return matching browser
     */
    public static BrowserType from(String value) {
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new FrameworkException("Unsupported browser '" + value + "'. Allowed: " + Arrays.toString(values()));
        }
    }
}
