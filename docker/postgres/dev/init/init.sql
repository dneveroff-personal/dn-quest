-- DN Quest - Database Initialization Script
-- Creates separate databases for each microservice

-- Authentication Service Database
CREATE DATABASE dnquest_auth;
GRANT ALL PRIVILEGES ON DATABASE dnquest_auth TO dn;

-- User Management Service Database
CREATE DATABASE dnquest_users;
GRANT ALL PRIVILEGES ON DATABASE dnquest_users TO dn;


-- Quest Management Service Database
CREATE DATABASE dnquest_quests;
GRANT ALL PRIVILEGES ON DATABASE dnquest_quests TO dn;

-- Game Engine Service Database
CREATE DATABASE dnquest_game;
GRANT ALL PRIVILEGES ON DATABASE dnquest_game TO dn;

-- Team Management Service Database
CREATE DATABASE dnquest_teams;
GRANT ALL PRIVILEGES ON DATABASE dnquest_teams TO dn;

-- Notification Service Database
CREATE DATABASE dnquest_notifications;
GRANT ALL PRIVILEGES ON DATABASE dnquest_notifications TO dn;

-- Statistics Service Database
CREATE DATABASE dnquest_statistics;
GRANT ALL PRIVILEGES ON DATABASE dnquest_statistics TO dn;


-- File Storage Service Database
CREATE DATABASE dnquest_files;
GRANT ALL PRIVILEGES ON DATABASE dnquest_files TO dn;

-- Connect to each database and create schemas
\c dnquest_auth;
CREATE SCHEMA IF NOT EXISTS auth;
GRANT ALL PRIVILEGES ON SCHEMA auth TO dn;
ALTER USER dn SET search_path TO auth, public;

\c dnquest_users;
CREATE SCHEMA IF NOT EXISTS users;
GRANT ALL PRIVILEGES ON SCHEMA users TO dn;
ALTER USER dn SET search_path TO users, public;

\c dnquest_quests;
CREATE SCHEMA IF NOT EXISTS quests;
GRANT ALL PRIVILEGES ON SCHEMA quests TO dn;
ALTER USER dn SET search_path TO quests, public;

\c dnquest_game;
CREATE SCHEMA IF NOT EXISTS game;
GRANT ALL PRIVILEGES ON SCHEMA game TO dn;
ALTER USER dn SET search_path TO game, public;

\c dnquest_teams;
CREATE SCHEMA IF NOT EXISTS teams;
GRANT ALL PRIVILEGES ON SCHEMA teams TO dn;
ALTER USER dn SET search_path TO teams, public;

\c dnquest_notifications;
CREATE SCHEMA IF NOT EXISTS notifications;
GRANT ALL PRIVILEGES ON SCHEMA notifications TO dn;
ALTER USER dn SET search_path TO notifications, public;

\c dnquest_statistics;
CREATE SCHEMA IF NOT EXISTS statistics;
GRANT ALL PRIVILEGES ON SCHEMA statistics TO dn;
ALTER USER dn SET search_path TO statistics, public;

\c dnquest_files;
CREATE SCHEMA IF NOT EXISTS files;
GRANT ALL PRIVILEGES ON SCHEMA files TO dn;
ALTER USER dn SET search_path TO files, public;
