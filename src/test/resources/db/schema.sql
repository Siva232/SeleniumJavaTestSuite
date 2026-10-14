-- One-time setup for the @db scenarios (MySQL 8+).
-- The scenarios also run CREATE TABLE IF NOT EXISTS, so only the database and user are required.

CREATE DATABASE IF NOT EXISTS demoproject;

USE demoproject;

CREATE TABLE IF NOT EXISTS api_posts (
    record_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id    INT          NOT NULL,
    user_id    INT          NOT NULL,
    title      VARCHAR(255) NOT NULL,
    body       TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
