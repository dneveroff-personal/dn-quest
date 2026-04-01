-- Development PostgreSQL initialization script
-- This script runs automatically on first container start for dev environment

-- Enable UUID extension if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Enable citext extension for case-insensitive text
CREATE EXTENSION IF NOT EXISTS "citext";

-- Enable pgcrypto for password hashing
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Log statements for debugging
SET log_statement = 'all';
SET log_min_duration_statement = 0;

-- Create all required databases for microservices
CREATE DATABASE dnquest_auth;
CREATE DATABASE dnquest_users;
CREATE DATABASE dnquest_quests;
CREATE DATABASE dnquest_game;
CREATE DATABASE dnquest_teams;
CREATE DATABASE dnquest_notifications;
CREATE DATABASE dnquest_statistics;
CREATE DATABASE dnquest_files;

-- Grant privileges to the default user
GRANT ALL PRIVILEGES ON DATABASE dnquest_auth TO dn;
GRANT ALL PRIVILEGES ON DATABASE dnquest_users TO dn;
GRANT ALL PRIVILEGES ON DATABASE dnquest_quests TO dn;
GRANT ALL PRIVILEGES ON DATABASE dnquest_game TO dn;
GRANT ALL PRIVILEGES ON DATABASE dnquest_teams TO dn;
GRANT ALL PRIVILEGES ON DATABASE dnquest_notifications TO dn;
GRANT ALL PRIVILEGES ON DATABASE dnquest_statistics TO dn;
GRANT ALL PRIVILEGES ON DATABASE dnquest_files TO dn;

-- Development-specific settings
DO $$ 
BEGIN
    -- Custom type initialization can be added here
    NULL;
END $$;
