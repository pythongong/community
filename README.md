# Community


# User

SQL:

```sql
CREATE DATABASE IF NOT EXISTS users
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE users;
CREATE TABLE community_user (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'Unique auto-incrementing integer identifier for each user',
    update_id INT NOT NULL COMMENT 'ID of the user who last updated this record',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT 'Timestamp of user record creation',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT 'Timestamp of last user record update',
    user_name VARCHAR(20) NOT NULL COMMENT 'Unique username (1-20 characters)',
    user_password VARCHAR(100) NOT NULL COMMENT 'User password (8-20 characters, store encrypted value)',
    gender TINYINT UNSIGNED NOT NULL COMMENT 'User gender (0: male, 1: female)',
    user_type ENUM('REGULAR', 'OFFICIAL', 'KOL', 'EMPLOYEE') DEFAULT 'REGULAR' NOT NULL COMMENT 'Type of the user (REGULAR, OFFICIAL, KOL, EMPLOYEE)',
    CONSTRAINT chk_gender_value CHECK (gender IN (0, 1)),
    UNIQUE INDEX uk_username (user_name),
    INDEX idx_user_type (user_type) 
) COMMENT = 'Stores information about registered users of the application.'
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_0900_ai_ci;
```