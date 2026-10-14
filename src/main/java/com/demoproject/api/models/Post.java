package com.demoproject.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

/**
 * Request/response POJO for the {@code /posts} resource. Null fields are omitted when serialised,
 * so the same class serves create (no id), update and partial (PATCH) payloads.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Post {

    private Integer id;
    private Integer userId;
    private String title;
    private String body;

    /** Required by Jackson. */
    public Post() {
    }

    /**
     * @param userId owner id
     * @param title  title
     * @param body   body text
     */
    public Post(Integer userId, String title, String body) {
        this.userId = userId;
        this.title = title;
        this.body = body;
    }

    /** @return post id (null for create requests) */
    public Integer getId() {
        return id;
    }

    /** @param id post id */
    public void setId(Integer id) {
        this.id = id;
    }

    /** @return owner id */
    public Integer getUserId() {
        return userId;
    }

    /** @param userId owner id */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /** @return title */
    public String getTitle() {
        return title;
    }

    /** @param title title */
    public void setTitle(String title) {
        this.title = title;
    }

    /** @return body text */
    public String getBody() {
        return body;
    }

    /** @param body body text */
    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Post other)) {
            return false;
        }
        return Objects.equals(id, other.id) && Objects.equals(userId, other.userId)
                && Objects.equals(title, other.title) && Objects.equals(body, other.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, title, body);
    }

    @Override
    public String toString() {
        return "Post{id=" + id + ", userId=" + userId + ", title='" + title + "'}";
    }
}
