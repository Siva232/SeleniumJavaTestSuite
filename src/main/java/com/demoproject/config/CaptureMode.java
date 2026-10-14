package com.demoproject.config;

import com.demoproject.exceptions.FrameworkException;

import java.util.Arrays;
import java.util.Locale;

/**
 * Controls when screenshots / videos are kept: only for failed scenarios, for all, or never.
 */
public enum CaptureMode {
    FAILED,
    ALL,
    NONE;

    /**
     * @param scenarioFailed whether the scenario failed
     * @return true if an artefact should be kept for a scenario with this outcome
     */
    public boolean shouldKeep(boolean scenarioFailed) {
        return this == ALL || (this == FAILED && scenarioFailed);
    }

    /**
     * @param value raw configuration value
     * @param key   configuration key, for error messages
     * @return parsed mode
     */
    public static CaptureMode from(String value, String key) {
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new FrameworkException("Invalid value '" + value + "' for '" + key + "'. Allowed: "
                    + Arrays.toString(values()));
        }
    }
}
