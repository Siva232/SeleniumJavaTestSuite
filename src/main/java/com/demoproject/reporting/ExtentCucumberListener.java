package com.demoproject.reporting;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.DataTableArgument;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.HookTestStep;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.StepArgument;
import io.cucumber.plugin.event.TestStepFinished;

import java.util.List;
import java.util.Optional;

/**
 * Cucumber plugin that writes every Gherkin step (and failing hooks) with its result into the
 * current scenario's Extent node. As a {@link ConcurrentEventListener} it receives events
 * synchronously on the thread executing the scenario, so the thread-local node is correct in
 * parallel runs.
 */
public class ExtentCucumberListener implements ConcurrentEventListener {

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestStepFinished.class, this::onStepFinished);
    }

    private void onStepFinished(TestStepFinished event) {
        Optional<ExtentTest> current = ExtentTestManager.currentTest();
        if (current.isEmpty()) {
            return;
        }
        ExtentTest test = current.get();
        Result result = event.getResult();

        if (event.getTestStep() instanceof PickleStepTestStep step) {
            String text = "<b>" + HtmlEscaper.escape(step.getStep().getKeyword().trim()) + "</b> "
                    + HtmlEscaper.escape(step.getStep().getText());
            logStep(test, result, text);
            logArgument(test, step.getStep().getArgument());
        } else if (event.getTestStep() instanceof HookTestStep hook && result.getStatus() == Status.FAILED) {
            logFailure(test, "Hook failed: " + HtmlEscaper.escape(hook.getCodeLocation()), result);
        }
    }

    private void logStep(ExtentTest test, Result result, String text) {
        switch (result.getStatus()) {
            case PASSED -> test.pass(text);
            case FAILED -> logFailure(test, text, result);
            case SKIPPED, UNUSED -> test.skip(text);
            case PENDING -> test.warning(text + " <i>(pending)</i>");
            case UNDEFINED -> {
                test.fail(text + " <i>(undefined step - no matching step definition)</i>");
                ExtentTestManager.recordFailure("Undefined step");
            }
            case AMBIGUOUS -> {
                test.fail(text + " <i>(ambiguous step definitions)</i>");
                ExtentTestManager.recordFailure("Ambiguous step");
            }
            default -> test.info(text);
        }
    }

    private void logFailure(ExtentTest test, String text, Result result) {
        test.fail(text);
        Throwable error = result.getError();
        if (error != null) {
            test.fail(error);
            String message = error.getMessage() == null ? "" : error.getMessage().lines().findFirst().orElse("");
            ExtentTestManager.recordFailure(error.getClass().getSimpleName() + ": " + message);
        } else {
            ExtentTestManager.recordFailure("Step failed");
        }
    }

    private void logArgument(ExtentTest test, StepArgument argument) {
        if (argument instanceof DataTableArgument table) {
            List<List<String>> cells = table.cells();
            String[][] data = cells.stream()
                    .map(row -> row.stream().map(HtmlEscaper::escape).toArray(String[]::new))
                    .toArray(String[][]::new);
            test.info(MarkupHelper.createTable(data));
        }
    }
}
