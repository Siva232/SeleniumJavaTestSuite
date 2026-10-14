package com.demoproject.api.client;

import com.demoproject.api.specs.SpecFactory;
import com.demoproject.utils.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import io.restassured.response.Response;
import org.testng.Assert;

import java.util.Objects;

/**
 * Fluent assertions over an API {@link Response}: status, headers, JSON path values and
 * Jackson deserialisation into POJOs.
 */
public final class ResponseValidator {

    private final Response response;

    private ResponseValidator(Response response) {
        this.response = Objects.requireNonNull(response, "No API response to validate - send a request first");
    }

    /**
     * @param response response to validate
     * @return validator
     */
    public static ResponseValidator of(Response response) {
        return new ResponseValidator(response);
    }

    /**
     * @param expected expected HTTP status code
     * @return this validator
     */
    public ResponseValidator statusCode(int expected) {
        response.then().spec(SpecFactory.responseSpec(expected));
        return this;
    }

    /**
     * @param name     header name (case-insensitive)
     * @param expected expected substring of the header value
     * @return this validator
     */
    public ResponseValidator headerContains(String name, String expected) {
        String actual = response.getHeader(name);
        Assert.assertNotNull(actual, "Response header '" + name + "' is missing");
        Assert.assertTrue(actual.contains(expected),
                "Header '" + name + "' was '" + actual + "', expected it to contain '" + expected + "'");
        return this;
    }

    /**
     * Compares the JSON path value as a string, so numbers and strings can both be checked.
     *
     * @param path     Rest Assured JSON path, e.g. {@code id} or {@code data[0].name}
     * @param expected expected value
     * @return this validator
     */
    public ResponseValidator jsonPathEquals(String path, String expected) {
        Object actual = response.jsonPath().get(path);
        Assert.assertNotNull(actual, "JSON path '" + path + "' not found in response");
        Assert.assertEquals(String.valueOf(actual), expected, "Unexpected value at JSON path '" + path + "'");
        return this;
    }

    /**
     * @param type POJO class
     * @param <T>  POJO type
     * @return the body deserialised with the shared Jackson mapper
     */
    public <T> T as(Class<T> type) {
        return JsonUtils.fromJson(response.asString(), type);
    }

    /**
     * @param type generic type, e.g. {@code new TypeReference<List<Post>>() {}}
     * @param <T>  target type
     * @return the body deserialised with the shared Jackson mapper
     */
    public <T> T as(TypeReference<T> type) {
        return JsonUtils.fromJson(response.asString(), type);
    }
}
