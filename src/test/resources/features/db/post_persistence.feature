@api @db
Feature: Database verification of records created through the API
  As a QA engineer
  I want to confirm that a post created through the API is stored in MySQL
  So that API and database stay consistent

  Background:
    Given the database table "db.post.table" exists

  @regression
  Scenario: Post created through the API is persisted and verified in MySQL
    When user sends a POST request to "api.posts.endpoint" with payload "api.post.create.payload"
    Then the response status code should be "api.status.created"
    And the response post should match the request payload
    When the created post is saved to the database table "db.post.table"
    Then the database table "db.post.table" should contain the created post
    When the created post is deleted from the database table "db.post.table"
    Then the database table "db.post.table" should not contain the created post
