package com.demoproject.reporting;

import com.demoproject.config.ConfigReader;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Counts how many times each scenario (keyed by {@code uri:line}) has started in this JVM,
 * so retries can be reported as attempts of one test rather than as duplicate tests.
 */
public final class ScenarioAttemptTracker {

    private static final Map<String, AtomicInteger> ATTEMPTS = new ConcurrentHashMap<>();

    private ScenarioAttemptTracker() {
    }

    /**
     * @param scenarioKey stable scenario identifier ({@code uri:line})
     * @return 1-based number of the attempt that is starting now
     */
    public static int nextAttempt(String scenarioKey) {
        return ATTEMPTS.computeIfAbsent(scenarioKey, k -> new AtomicInteger()).incrementAndGet();
    }

    /**
     * @return total attempts allowed per scenario: 1 + {@code rerun.count}
     */
    public static int maxAttempts() {
        return ConfigReader.getInstance().getRerunCount() + 1;
    }
}
