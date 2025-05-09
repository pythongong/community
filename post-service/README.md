
# SQL:

```sql
CREATE DATABASE IF NOT EXISTS post
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE post;
CREATE TABLE community_post (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Unique auto-incrementing integer identifier for each post',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT 'Timestamp of creation',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT 'Timestamp of last update',
    deleted BOOLEAN DEFAULT FALSE NOT NULL COMMENT 'Flag indicating whether the record has been deleted',
    create_user_id BIGINT NOT NULL COMMENT 'ID of the user who created the post',
    update_user_id BIGINT NOT NULL COMMENT 'ID of the user who last updated the post',
    content VARCHAR(1000) NOT NULL COMMENT 'Content of the post. 1-1000 characters',
    tag VARCHAR(20) NULL COMMENT 'Tag of the post. Maximum 20 characters',
    post_status VARCHAR(20) NOT NULL COMMENT 'Status of the post. Maximum 20 characters. DRAFT, PUBLISHED, BANNED',
    post_type VARCHAR(20) NOT NULL COMMENT 'Type of the post. Maximum 20 characters. TEXT, ACTIVITY, QUESTION',
    post_url VARCHAR(255) NOT NULL COMMENT 'URL of the activity or question post. Maximum 255 characters',
    INDEX idx_create_user_id (create_user_id),
    INDEX idx_post_status (post_status),
    INDEX idx_post_type (post_type),
    INDEX idx_tag (tag)
) COMMENT = 'Stores information about posts in the community.'
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_0900_ai_ci;
```