-- Base initialization script for PostgreSQL
-- This script runs automatically on first container start

-- Enable UUID extension if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Enable citext extension for case-insensitive text
CREATE EXTENSION IF NOT EXISTS "citext";

-- Create custom types if needed
DO $$ 
BEGIN
    -- Custom type initialization can be added here
    NULL;
END $$;
