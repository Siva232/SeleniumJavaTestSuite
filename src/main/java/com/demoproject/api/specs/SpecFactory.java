package com.demoproject.api.specs;

import com.demoproject.api.filters.ExtentLoggingFilter;
import com.demoproject.config.ConfigReader;
import com.demoproject.utils.JsonUtils;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

/**
 * Builds Rest Assured request/response specifications from configuration: base URI, JSON
 * headers, optional bearer auth, timeouts, the shared Jackson mapper and the Extent logging filter.
 */
public final class SpecFactory {

    private SpecFactory() {
    }

    /**
     * @return a fresh request specification (specs are mutable, so one is built per request)
     */
    public static RequestSpecification requestSpec() {
        ConfigReader config = ConfigReader.getInstance();
        RequestSpecBuilder builder = new RequestSpecBuilder()
                .setBaseUri(config.getApiBaseUrl())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addHeader("X-Client", "DemoProject")
                .setConfig(restAssuredConfig(config.getApiTimeoutSeconds()))
                .addFilter(new ExtentLoggingFilter());
        config.getApiAuthToken().ifPresent(token -> builder.addHeader("Authorization", "Bearer " + token));
        return builder.build();
    }

    /**
     * @param expectedStatus expected HTTP status
     * @return response specification asserting the status code
     */
    public static ResponseSpecification responseSpec(int expectedStatus) {
        return new ResponseSpecBuilder().expectStatusCode(expectedStatus).build();
    }

    /**
     * @param expectedStatus expected HTTP status
     * @return response specification asserting the status code and a JSON content type
     */
    public static ResponseSpecification jsonResponseSpec(int expectedStatus) {
        return new ResponseSpecBuilder().expectStatusCode(expectedStatus).expectContentType(ContentType.JSON).build();
    }

    private static RestAssuredConfig restAssuredConfig(int timeoutSeconds) {
        int timeoutMs = timeoutSeconds * 1000;
        return RestAssuredConfig.config()
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", timeoutMs)
                        .setParam("http.socket.timeout", timeoutMs)
                        .setParam("http.connection-manager.timeout", (long) timeoutMs))
                .objectMapperConfig(new ObjectMapperConfig()
                        .jackson2ObjectMapperFactory((type, charset) -> JsonUtils.mapper()));
    }
}
