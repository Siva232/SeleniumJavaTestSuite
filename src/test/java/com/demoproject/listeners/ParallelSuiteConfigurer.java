package com.demoproject.listeners;

import com.demoproject.config.ConfigReader;
import org.testng.IAlterSuiteListener;
import org.testng.xml.XmlSuite;

import java.util.List;
import java.util.logging.Logger;

/**
 * Injects {@code parallel.thread.count} into TestNG as the data-provider thread count before
 * the suite starts, so the value is not hard-coded in testng.xml.
 */
public class ParallelSuiteConfigurer implements IAlterSuiteListener {

    private static final Logger LOG = Logger.getLogger(ParallelSuiteConfigurer.class.getName());

    @Override
    public void alter(List<XmlSuite> suites) {
        int threads = ConfigReader.getInstance().getParallelThreadCount();
        for (XmlSuite suite : suites) {
            suite.setDataProviderThreadCount(threads);
        }
        LOG.info(() -> "Parallel data-provider thread count set to " + threads);
    }
}
