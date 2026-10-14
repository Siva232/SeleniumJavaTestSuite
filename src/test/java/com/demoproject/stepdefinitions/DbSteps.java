package com.demoproject.stepdefinitions;

import com.demoproject.api.models.Post;
import com.demoproject.context.ScenarioContext;
import com.demoproject.db.DbUtils;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

import java.util.List;
import java.util.Map;

/**
 * MySQL steps. Table names come from test data and are validated by {@link DbUtils#identifier(String)};
 * every value is bound through a PreparedStatement parameter.
 */
public class DbSteps extends BaseSteps {

    @Given("the database table {string} exists")
    public void theDatabaseTableExists(String tableKey) {
        DbUtils.update("CREATE TABLE IF NOT EXISTS " + table(tableKey) + " ("
                + "record_id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                + "post_id INT NOT NULL, "
                + "user_id INT NOT NULL, "
                + "title VARCHAR(255) NOT NULL, "
                + "body TEXT, "
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
    }

    @When("the created post is saved to the database table {string}")
    public void theCreatedPostIsSavedToTheDatabaseTable(String tableKey) {
        Post post = createdPost();
        int rows = DbUtils.update("INSERT INTO " + table(tableKey) + " (post_id, user_id, title, body) VALUES (?, ?, ?, ?)",
                post.getId(), post.getUserId(), post.getTitle(), post.getBody());
        Assert.assertEquals(rows, 1, "Inserted rows");
    }

    @Then("the database table {string} should contain the created post")
    public void theDatabaseTableShouldContainTheCreatedPost(String tableKey) {
        Post post = createdPost();
        List<Map<String, Object>> rows = findPost(tableKey, post);
        Assert.assertEquals(rows.size(), 1, "Rows matching the created post");
        Map<String, Object> row = rows.get(0);
        Assert.assertEquals(((Number) row.get("user_id")).intValue(), post.getUserId().intValue(), "user_id");
        Assert.assertEquals(row.get("body"), post.getBody(), "body");
    }

    @When("the created post is deleted from the database table {string}")
    public void theCreatedPostIsDeletedFromTheDatabaseTable(String tableKey) {
        Post post = createdPost();
        DbUtils.update("DELETE FROM " + table(tableKey) + " WHERE post_id = ? AND title = ?", post.getId(), post.getTitle());
    }

    @Then("the database table {string} should not contain the created post")
    public void theDatabaseTableShouldNotContainTheCreatedPost(String tableKey) {
        Assert.assertTrue(findPost(tableKey, createdPost()).isEmpty(), "Created post is still in the table");
    }

    private List<Map<String, Object>> findPost(String tableKey, Post post) {
        return DbUtils.query("SELECT post_id, user_id, title, body FROM " + table(tableKey)
                + " WHERE post_id = ? AND title = ?", post.getId(), post.getTitle());
    }

    private String table(String tableKey) {
        return DbUtils.identifier(data(tableKey));
    }

    private Post createdPost() {
        return ScenarioContext.get(ScenarioContext.Key.RESPONSE_POST, Post.class);
    }
}
