-- Create the database if it doesn't exist
-- Note: Character set and collation are typically configured at the database or cluster level in PostgreSQL
CREATE DATABASE IF NOT EXISTS user_management;

-- Connect to the database (This is a psql command, not standard SQL.
-- You would typically connect to the database directly when running the script.)
-- \c user_management;
DROP TABLE IF EXISTS community_user;

-- Create the community_user table
CREATE TABLE community_user (
    id BIGSERIAL PRIMARY KEY, -- Unique auto-incrementing integer identifier for each user
    create_time TIMESTAMP NOT NULL, -- Timestamp of user record creation
    update_time TIMESTAMP NOT NULL, -- Timestamp of last user record update, stored with time zone
    deleted BOOLEAN DEFAULT FALSE NOT NULL, -- Flag indicating whether the user record has been deleted
    user_name VARCHAR(20) UNIQUE NOT NULL, -- Unique username (1-20 characters)
    user_password VARCHAR(100) NOT NULL, -- User password (8-20 characters, store encrypted value)
    nick_name VARCHAR(20) NOT NULL, -- Repeatable nick username (1-20 characters)
    avatar VARCHAR(255) NOT NULL, -- User avatar (URL)
    gender SMALLINT NOT NULL, -- User gender (0: male, 1: female)
    user_type VARCHAR(20) DEFAULT 'REGULAR' NOT NULL, -- Type of the user (REGULAR, OFFICIAL, KOL)
    user_status VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL, -- User status (ACTIVE, INACTIVE, BANNED)
    user_profile VARCHAR(100), -- User profile (max 100 characters)
);

-- Create indexes
CREATE INDEX idx_nick_name ON community_user (nick_name);
CREATE INDEX idx_user_type ON community_user (user_type);
CREATE INDEX idx_user_status ON community_user (user_status);

-- Add comments for the table and columns
COMMENT ON TABLE community_user IS 'Stores information about registered users of the application.';
COMMENT ON COLUMN community_user.id IS 'Unique auto-incrementing integer identifier for each user';
COMMENT ON COLUMN community_user.create_time IS 'Timestamp of user record creation';
COMMENT ON COLUMN community_user.update_time IS 'Timestamp of last user record update (stored with time zone)';
COMMENT ON COLUMN community_user.deleted IS 'Flag indicating whether the user record has been deleted';
COMMENT ON COLUMN community_user.user_name IS 'Unique username (1-20 characters)';
COMMENT ON COLUMN community_user.user_password IS 'User password (8-20 characters, store encrypted value)';
COMMENT ON COLUMN community_user.nick_name IS 'Repeatable nick username (1-20 characters)';
COMMENT ON COLUMN community_user.avatar IS 'User avatar (URL)';
COMMENT ON COLUMN community_user.user_profile IS 'User profile (max 100 characters)';
COMMENT ON COLUMN community_user.gender IS 'User gender (0: male, 1: female)';
COMMENT ON COLUMN community_user.user_type IS 'Type of the user (REGULAR, OFFICIAL, KOL)';
COMMENT ON COLUMN community_user.user_status IS 'User status (ACTIVE, INACTIVE, BANNED)';