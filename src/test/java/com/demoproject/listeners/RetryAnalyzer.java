package com.demoproject.listeners;

import com.demoproject.config.ConfigReader;
import io.cucumber.testng.PickleWrapper;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Retries a failed scenario up to {@code rerun.count} times. Counts are kept per scenario
 * ({@code uri:line}) because one data-provider test method runs every scenario.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger LOG = Logger.getLogger(RetryAnalyzer.class.getName());
    private static final Map<String, AtomicInteger> RETRIES = new ConcurrentHashMap<>();

    @Override
    public boolean retry(ITestResult result) {
        int maxRetries = ConfigReader.getInstance().getRerunCount();
        if (maxRetries <= 0) {
            return false;
        }
        String key = scenarioKey(result);
        int retriesSoFar = RETRIES.computeIfAbsent(key, k -> new AtomicInteger()).get();
        if (retriesSoFar >= maxRetries) {
            return false;
        }
        int attempt = RETRIES.get(key).incrementAndGet();
        LOG.warning(() -> "Retrying '" + scenarioName(result) + "' (retry " + attempt + " of " + maxRetries + ")");
        return true;
    }

    private static String scenarioKey(ITestResult result) {
        Object[] params = result.getParameters();
        if (params.length > 0 && params[0] instanceof PickleWrapper wrapper) {
            return wrapper.getPickle().getUri() + ":" + wrapper.getPickle().getLine();
        }
        return result.getMethod().getQualifiedName() + Arrays.toString(params);
    }

    private static String scenarioName(ITestResult result) {
        Object[] params = result.getParameters();
        if (params.length > 0 && params[0] instanceof PickleWrapper wrapper) {
            return wrapper.getPickle().getName();
        }
        return result.getName();
    }
}
