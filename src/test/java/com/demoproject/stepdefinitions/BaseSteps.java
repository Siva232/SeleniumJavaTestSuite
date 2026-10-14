package com.demoproject.stepdefinitions;

import com.demoproject.testdata.TestDataReader;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Shared helpers for step definitions: every value coming from a feature file is resolved
 * through {@link TestDataReader} (a known key yields its value, anything else is a literal).
 */
public abstract class BaseSteps {

    /**
     * @param keyOrLiteral test data key or literal
     * @return resolved value
     */
    protected String data(String keyOrLiteral) {
        return TestDataReader.getInstance().resolve(keyOrLiteral);
    }

    /**
     * @param keyOrLiteral test data key or literal number
     * @return resolved integer
     */
    protected int dataInt(String keyOrLiteral) {
        return TestDataReader.getInstance().resolveInt(keyOrLiteral);
    }

    /**
     * @param keysOrLiterals list from a Gherkin data table
     * @return resolved values, same order
     */
    protected List<String> dataList(List<String> keysOrLiterals) {
        return keysOrLiterals.stream().map(this::data).collect(Collectors.toList());
    }
}
