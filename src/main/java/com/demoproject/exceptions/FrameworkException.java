package com.demoproject.exceptions;

/**
 * Unchecked exception raised for framework-level problems (configuration, test data,
 * driver creation, database access, reporting). Messages are written to be actionable.
 */
public class FrameworkException extends RuntimeException {

    /**
     * @param message human-readable description of what went wrong and how to fix it
     */
    public FrameworkException(String message) {
        super(message);
    }

    /**
     * @param message human-readable description of what went wrong and how to fix it
     * @param cause   underlying exception
     */
    public FrameworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
