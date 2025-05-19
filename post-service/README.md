
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
    create_user_id BIGINT NOT NULL COMMENT 'ID of the user who created the post',
    update_user_id BIGINT NOT NULL COMMENT 'ID of the user who last updated the post',
    content VARCHAR(1000) NOT NULL COMMENT 'Content of the post. 1-1000 characters',
    post_status VARCHAR(20) NOT NULL COMMENT 'Status of the post. Maximum 20 characters. DRAFT, PUBLISHED, BANNED',
    post_type VARCHAR(20) NOT NULL COMMENT 'Type of the post. Maximum 20 characters. TEXT, ACTIVITY, QUESTION',
    INDEX idx_post_status (post_status),
    INDEX idx_post_type (post_type),
    INDEX idx_tag (tag)
) COMMENT = 'Stores information about posts in the community.'
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_0900_ai_ci;
```