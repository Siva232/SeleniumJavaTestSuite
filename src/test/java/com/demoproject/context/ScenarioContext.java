package com.demoproject.context;

import com.demoproject.exceptions.FrameworkException;

import java.util.HashMap;
import java.util.Map;

/**
 * Per-scenario state shared between step definition classes (one map per thread, cleared after
 * every scenario). Used instead of a DI container because only the allowed dependencies may be used.
 */
public final class ScenarioContext {

    /** Keys used across step classes. */
    public enum Key {
        API_RESPONSE,
        REQUEST_POST,
        RESPONSE_POST,
        PARENT_WINDOW,
        ALERT_NAME
    }

    private static final ThreadLocal<Map<Key, Object>> STATE = ThreadLocal.withInitial(HashMap::new);

    private ScenarioContext() {
    }

    /**
     * @param key   entry key
     * @param value value to store
     */
    public static void put(Key key, Object value) {
        STATE.get().put(key, value);
    }

    /**
     * @param key  entry key
     * @param type expected type
     * @param <T>  expected type
     * @return stored value
     * @throws FrameworkException if nothing was stored under the key in this scenario
     */
    public static <T> T get(Key key, Class<T> type) {
        Object value = STATE.get().get(key);
        if (value == null) {
            throw new FrameworkException("Nothing stored in the scenario context under " + key
                    + " - check the step order of the scenario");
        }
        return type.cast(value);
    }

    /** Clears the current thread's state. */
    public static void clear() {
        STATE.remove();
    }
}
