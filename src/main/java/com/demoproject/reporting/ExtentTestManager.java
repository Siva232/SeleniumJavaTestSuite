package com.demoproject.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.demoproject.exceptions.FrameworkException;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-local access to the current scenario's {@link ExtentTest}.
 * <p>
 * Retry handling: when a scenario starts again, the node of its previous attempt is removed
 * from the report and the new node records the attempt number and the previous failure, so the
 * report shows each scenario once with its final result.
 */
public final class ExtentTestManager {

    private static final ThreadLocal<ExtentTest> CURRENT = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_KEY = new ThreadLocal<>();
    private static final ThreadLocal<Integer> CURRENT_ATTEMPT = new ThreadLocal<>();
    private static final ThreadLocal<String> LAST_FAILURE = new ThreadLocal<>();
    private static final Map<String, ExtentTest> BY_SCENARIO = new ConcurrentHashMap<>();
    private static final Map<String, String> PREVIOUS_FAILURES = new ConcurrentHashMap<>();

    private ExtentTestManager() {
    }

    /**
     * Creates the report node for a scenario attempt and binds it to the current thread.
     *
     * @param scenarioKey stable scenario id ({@code uri:line})
     * @param name        scenario name
     * @param tags        Cucumber tags, added as categories
     * @param attempt     1-based attempt number
     * @param maxAttempts total allowed attempts
     * @return the new node
     */
    public static ExtentTest startTest(String scenarioKey, String name, Collection<String> tags,
                                       int attempt, int maxAttempts) {
        ExtentReports extent = ExtentManager.getInstance();
        ExtentTest test;
        synchronized (ExtentManager.lock()) {
            ExtentTest previous = BY_SCENARIO.remove(scenarioKey);
            if (previous != null) {
                extent.removeTest(previous);
            }
            test = extent.createTest(name);
        }
        tags.stream().map(t -> t.startsWith("@") ? t.substring(1) : t).forEach(test::assignCategory);
        if (attempt > 1) {
            test.info(MarkupHelper.createLabel("Retry attempt " + (attempt - 1) + " of " + (maxAttempts - 1),
                    ExtentColor.ORANGE));
            String previousFailure = PREVIOUS_FAILURES.getOrDefault(scenarioKey, "unknown failure");
            test.info("Previous attempt failed: " + HtmlEscaper.escape(previousFailure));
        }
        BY_SCENARIO.put(scenarioKey, test);
        CURRENT.set(test);
        CURRENT_KEY.set(scenarioKey);
        CURRENT_ATTEMPT.set(attempt);
        LAST_FAILURE.remove();
        return test;
    }

    /**
     * @return the current thread's node
     * @throws FrameworkException if no scenario is active on this thread
     */
    public static ExtentTest getTest() {
        ExtentTest test = CURRENT.get();
        if (test == null) {
            throw new FrameworkException("No Extent test is bound to thread " + Thread.currentThread().getName()
                    + "; was the scenario started by Hooks?");
        }
        return test;
    }

    /**
     * @return the current thread's node, if a scenario is active
     */
    public static Optional<ExtentTest> currentTest() {
        return Optional.ofNullable(CURRENT.get());
    }

    /**
     * Remembers the first failure of the running attempt (shown on the next retry).
     *
     * @param message short failure description
     */
    public static void recordFailure(String message) {
        if (LAST_FAILURE.get() == null) {
            LAST_FAILURE.set(message);
        }
    }

    /**
     * Writes the final outcome line and unbinds the node from the thread.
     *
     * @param failed      whether the scenario failed
     * @param status      Cucumber status name, e.g. PASSED
     * @param maxAttempts total allowed attempts
     */
    public static void endTest(boolean failed, String status, int maxAttempts) {
        ExtentTest test = CURRENT.get();
        if (test == null) {
            return;
        }
        int attempt = Optional.ofNullable(CURRENT_ATTEMPT.get()).orElse(1);
        try {
            if (failed) {
                PREVIOUS_FAILURES.put(CURRENT_KEY.get(),
                        Optional.ofNullable(LAST_FAILURE.get()).orElse("status " + status));
                if (attempt < maxAttempts) {
                    test.warning("Attempt " + attempt + " of " + maxAttempts + " failed; the scenario will be retried");
                } else {
                    test.fail(MarkupHelper.createLabel("FAILED after " + attempt + " attempt(s)", ExtentColor.RED));
                }
            } else if ("PASSED".equals(status)) {
                String label = attempt > 1
                        ? "PASSED on attempt " + attempt + " (" + (attempt - 1) + " retr" + (attempt == 2 ? "y" : "ies") + ")"
                        : "PASSED";
                test.pass(MarkupHelper.createLabel(label, ExtentColor.GREEN));
            } else {
                test.skip(MarkupHelper.createLabel(status, ExtentColor.GREY));
            }
        } finally {
            CURRENT.remove();
            CURRENT_KEY.remove();
            CURRENT_ATTEMPT.remove();
            LAST_FAILURE.remove();
        }
    }
}
