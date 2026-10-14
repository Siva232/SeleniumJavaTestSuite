package com.demoproject.runners;

import com.demoproject.core.FrameworkBootstrap;
import com.demoproject.reporting.ExtentManager;
import com.demoproject.reporting.ReportContext;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;

import java.util.logging.Logger;

/**
 * Cucumber TestNG runner. Scenarios are supplied through a parallel data provider; the thread
 * count is injected at runtime by {@code ParallelSuiteConfigurer} from {@code parallel.thread.count}.
 * Tags can be overridden with {@code -Dcucumber.filter.tags="..."}.
 */
@CucumberOptions(
        features = "classpath:features",
        glue = {"com.demoproject.stepdefinitions", "com.demoproject.hooks"},
        tags = "not @ignore",
        plugin = {
                "pretty",
                "summary",
                "html:target/cucumber-reports/cucumber.html",
                "json:target/cucumber-reports/cucumber.json",
                "com.demoproject.reporting.ExtentCucumberListener"
        },
        monochrome = true)
public class TestRunner extends AbstractTestNGCucumberTests {

    private static final Logger LOG = Logger.getLogger(TestRunner.class.getName());

    /** Validates config and prepares logging/reporting before any scenario runs. */
    @BeforeSuite(alwaysRun = true)
    public void bootstrap() {
        FrameworkBootstrap.initialize();
    }

    /**
     * @return one row per scenario, consumed by parallel TestNG workers
     */
    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }

    /** Writes the Extent report. */
    @AfterSuite(alwaysRun = true)
    public void flushReport() {
        ExtentManager.flush();
        LOG.info(() -> "Extent report: " + ReportContext.getInstance().getReportFile().toUri());
    }
}
