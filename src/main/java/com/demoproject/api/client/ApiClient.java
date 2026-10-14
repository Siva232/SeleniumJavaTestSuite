package com.demoproject.api.client;

import com.demoproject.api.specs.SpecFactory;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

/**
 * Thin, stateless wrapper over Rest Assured for GET/POST/PUT/PATCH/DELETE with optional path
 * parameters, query parameters and a body (any POJO, serialised by the shared Jackson mapper).
 */
public class ApiClient {

    /**
     * @param path        resource path, may contain {@code {name}} templates
     * @param pathParams  values for the templates
     * @param queryParams query string parameters
     * @return the response
     */
    public Response get(String path, Map<String, ?> pathParams, Map<String, ?> queryParams) {
        return send(Method.GET, path, pathParams, queryParams, null);
    }

    /**
     * @param path resource path
     * @param body request body (POJO or String)
     * @return the response
     */
    public Response post(String path, Object body) {
        return send(Method.POST, path, Map.of(), Map.of(), body);
    }

    /**
     * @param path       resource path
     * @param pathParams values for path templates
     * @param body       request body
     * @return the response
     */
    public Response put(String path, Map<String, ?> pathParams, Object body) {
        return send(Method.PUT, path, pathParams, Map.of(), body);
    }

    /**
     * @param path       resource path
     * @param pathParams values for path templates
     * @param body       partial request body
     * @return the response
     */
    public Response patch(String path, Map<String, ?> pathParams, Object body) {
        return send(Method.PATCH, path, pathParams, Map.of(), body);
    }

    /**
     * @param path       resource path
     * @param pathParams values for path templates
     * @return the response
     */
    public Response delete(String path, Map<String, ?> pathParams) {
        return send(Method.DELETE, path, pathParams, Map.of(), null);
    }

    /**
     * Generic request.
     *
     * @param method      HTTP method
     * @param path        resource path
     * @param pathParams  values for path templates (may be empty)
     * @param queryParams query parameters (may be empty)
     * @param body        request body or null
     * @return the response
     */
    public Response send(Method method, String path, Map<String, ?> pathParams, Map<String, ?> queryParams,
                         Object body) {
        RequestSpecification request = RestAssured.given().spec(SpecFactory.requestSpec());
        if (pathParams != null && !pathParams.isEmpty()) {
            request.pathParams(pathParams);
        }
        if (queryParams != null && !queryParams.isEmpty()) {
            request.queryParams(queryParams);
        }
        if (body != null) {
            request.body(body);
        }
        return request.when().request(method, path).then().extract().response();
    }
}
