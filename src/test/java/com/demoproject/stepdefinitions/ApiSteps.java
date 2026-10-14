package com.demoproject.stepdefinitions;

import com.demoproject.api.client.ApiClient;
import com.demoproject.api.client.ResponseValidator;
import com.demoproject.api.models.Post;
import com.demoproject.context.ScenarioContext;
import com.demoproject.testdata.PayloadReader;
import com.fasterxml.jackson.core.type.TypeReference;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.testng.Assert;

import java.util.List;
import java.util.Map;

/**
 * REST steps for the /posts resource. Endpoints, ids and payload file names come from test data.
 */
public class ApiSteps extends BaseSteps {

    private final ApiClient client = new ApiClient();

    @When("user sends a GET request to {string} with path parameter {string} as {string}")
    public void getWithPathParam(String endpointKey, String paramName, String valueKey) {
        store(client.get(data(endpointKey), Map.of(paramName, data(valueKey)), Map.of()));
    }

    @When("user sends a GET request to {string} with query parameter {string} as {string}")
    public void getWithQueryParam(String endpointKey, String paramName, String valueKey) {
        store(client.get(data(endpointKey), Map.of(), Map.of(paramName, data(valueKey))));
    }

    @When("user sends a POST request to {string} with payload {string}")
    public void postWithPayload(String endpointKey, String payloadKey) {
        Post request = loadPayload(payloadKey);
        store(client.post(data(endpointKey), request));
    }

    @When("user sends a PUT request to {string} with path parameter {string} as {string} and payload {string}")
    public void putWithPayload(String endpointKey, String paramName, String valueKey, String payloadKey) {
        Post request = loadPayload(payloadKey);
        store(client.put(data(endpointKey), Map.of(paramName, data(valueKey)), request));
    }

    @When("user sends a PATCH request to {string} with path parameter {string} as {string} and payload {string}")
    public void patchWithPayload(String endpointKey, String paramName, String valueKey, String payloadKey) {
        Post request = loadPayload(payloadKey);
        store(client.patch(data(endpointKey), Map.of(paramName, data(valueKey)), request));
    }

    @When("user sends a DELETE request to {string} with path parameter {string} as {string}")
    public void deleteWithPathParam(String endpointKey, String paramName, String valueKey) {
        store(client.delete(data(endpointKey), Map.of(paramName, data(valueKey))));
    }

    @Then("the response status code should be {string}")
    public void theResponseStatusCodeShouldBe(String statusKey) {
        validator().statusCode(dataInt(statusKey));
    }

    @Then("the response header {string} should contain {string}")
    public void theResponseHeaderShouldContain(String headerName, String valueKey) {
        validator().headerContains(data(headerName), data(valueKey));
    }

    @Then("the response JSON path {string} should be {string}")
    public void theResponseJsonPathShouldBe(String jsonPath, String valueKey) {
        validator().jsonPathEquals(jsonPath, data(valueKey));
    }

    @Then("the response should deserialize into a post with id {string} and user id {string}")
    public void theResponseShouldDeserializeIntoAPost(String idKey, String userIdKey) {
        Post post = validator().as(Post.class);
        Assert.assertEquals(post.getId(), Integer.valueOf(dataInt(idKey)), "Post id");
        Assert.assertEquals(post.getUserId(), Integer.valueOf(dataInt(userIdKey)), "Post userId");
        Assert.assertNotNull(post.getTitle(), "Post title");
        Assert.assertFalse(post.getTitle().isBlank(), "Post title is blank");
    }

    @Then("the response should contain {string} posts for user id {string}")
    public void theResponseShouldContainPostsForUser(String countKey, String userIdKey) {
        List<Post> posts = validator().as(new TypeReference<List<Post>>() { });
        Assert.assertEquals(posts.size(), dataInt(countKey), "Number of posts");
        Integer userId = dataInt(userIdKey);
        posts.forEach(p -> Assert.assertEquals(p.getUserId(), userId, "userId of post " + p.getId()));
    }

    @Then("the response post should match the request payload")
    public void theResponsePostShouldMatchTheRequestPayload() {
        Post request = ScenarioContext.get(ScenarioContext.Key.REQUEST_POST, Post.class);
        Post response = validator().as(Post.class);
        ScenarioContext.put(ScenarioContext.Key.RESPONSE_POST, response);
        if (request.getTitle() != null) {
            Assert.assertEquals(response.getTitle(), request.getTitle(), "title");
        }
        if (request.getBody() != null) {
            Assert.assertEquals(response.getBody(), request.getBody(), "body");
        }
        if (request.getUserId() != null) {
            Assert.assertEquals(response.getUserId(), request.getUserId(), "userId");
        }
        Assert.assertNotNull(response.getId(), "Response post id");
    }

    private Post loadPayload(String payloadKey) {
        Post request = PayloadReader.read(data(payloadKey), Post.class);
        ScenarioContext.put(ScenarioContext.Key.REQUEST_POST, request);
        return request;
    }

    private void store(Response response) {
        ScenarioContext.put(ScenarioContext.Key.API_RESPONSE, response);
    }

    private ResponseValidator validator() {
        return ResponseValidator.of(ScenarioContext.get(ScenarioContext.Key.API_RESPONSE, Response.class));
    }
}
