@api @posts
Feature: Posts REST API
  As an API consumer
  I want to read and write posts
  So that clients can manage content through the API

  @smoke
  Scenario: Retrieve a single post by id
    When user sends a GET request to "api.posts.by.id" with path parameter "id" as "api.post.existing.id"
    Then the response status code should be "api.status.ok"
    And the response header "Content-Type" should contain "api.content.type.json"
    And the response JSON path "id" should be "api.post.existing.id"
    And the response should deserialize into a post with id "api.post.existing.id" and user id "api.post.existing.userId"

  @regression
  Scenario: Retrieve posts filtered by user id
    When user sends a GET request to "api.posts.endpoint" with query parameter "userId" as "api.post.filter.userId"
    Then the response status code should be "api.status.ok"
    And the response should contain "api.post.filter.count" posts for user id "api.post.filter.userId"

  @smoke
  Scenario: Create a post from a JSON payload
    When user sends a POST request to "api.posts.endpoint" with payload "api.post.create.payload"
    Then the response status code should be "api.status.created"
    And the response post should match the request payload
    And the response JSON path "id" should be "api.post.created.id"

  @regression
  Scenario: Replace a post with PUT
    When user sends a PUT request to "api.posts.by.id" with path parameter "id" as "api.post.existing.id" and payload "api.post.update.payload"
    Then the response status code should be "api.status.ok"
    And the response post should match the request payload

  @regression
  Scenario: Partially update a post with PATCH
    When user sends a PATCH request to "api.posts.by.id" with path parameter "id" as "api.post.existing.id" and payload "api.post.patch.payload"
    Then the response status code should be "api.status.ok"
    And the response post should match the request payload
    And the response JSON path "id" should be "api.post.existing.id"

  @regression
  Scenario: Delete a post
    When user sends a DELETE request to "api.posts.by.id" with path parameter "id" as "api.post.existing.id"
    Then the response status code should be "api.status.ok"
