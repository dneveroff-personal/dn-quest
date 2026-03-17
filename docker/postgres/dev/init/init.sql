-- Development PostgreSQL initialization script
-- This script runs automatically on first container start for dev environment

-- Enable UUID extension if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Enable citext extension for case-insensitive text
CREATE EXTENSION IF NOT EXISTS "citeix";

-- Enable pgcrypto for password hashing
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Log statements for debugging
SET log_statement = 'all';
SET log_min_duration_statement = 0;

-- Development-specific settings
DO $$ 
BEGIN
    -- Custom type initialization can be added here
    NULL;
END $$;
