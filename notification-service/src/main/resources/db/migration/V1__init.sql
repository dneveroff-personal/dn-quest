-- Flyway migration for notification-service
-- Version: V1__init.sql
-- Description: Initial schema for notification service

-- Drop existing tables if they exist
DROP TABLE IF EXISTS notifications.notification_queue CASCADE;
DROP TABLE IF EXISTS notifications.notification_templates CASCADE;
DROP TABLE IF EXISTS notifications.user_notification_preferences CASCADE;
DROP TABLE IF EXISTS notifications.notifications CASCADE;

-- ============================================================================
-- Table: notifications
-- Main table for storing all notifications
-- ============================================================================
CREATE TABLE IF NOT EXISTS notifications.notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notification_id VARCHAR(64) NOT NULL UNIQUE,
    user_id UUID NOT NULL,
    recipient_email VARCHAR(255),
    recipient_phone VARCHAR(20),
    telegram_chat_id VARCHAR(50),
    fcm_token VARCHAR(255),
    type VARCHAR(20) NOT NULL,
    category VARCHAR(20) NOT NULL,
    priority VARCHAR(10) NOT NULL,
    subject VARCHAR(255),
    content TEXT,
    html_content TEXT,
    template_data JSON,
    related_entity_id VARCHAR(64),
    related_entity_type VARCHAR(50),
    source_event_id VARCHAR(64),
    source_event_type VARCHAR(50),
    status VARCHAR(20) NOT NULL,
    retry_count INTEGER NOT NULL DEFAULT 0,
    max_retries INTEGER NOT NULL DEFAULT 3,
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    scheduled_at TIMESTAMP WITH TIME ZONE,
    sent_at TIMESTAMP WITH TIME ZONE,
    delivered_at TIMESTAMP WITH TIME ZONE,
    read_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    correlation_id VARCHAR(64),
    metadata JSON
);

CREATE INDEX IF NOT EXISTS idx_notification_user_id ON notifications.notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notification_status ON notifications.notifications(status);
CREATE INDEX IF NOT EXISTS idx_notification_type ON notifications.notifications(type);
CREATE INDEX IF NOT EXISTS idx_notification_category ON notifications.notifications(category);
CREATE INDEX IF NOT EXISTS idx_notification_created_at ON notifications.notifications(created_at);
CREATE INDEX IF NOT EXISTS idx_notification_scheduled_at ON notifications.notifications(scheduled_at);

-- ============================================================================
-- Table: notification_templates
-- Table for storing notification templates
-- ============================================================================
CREATE TABLE IF NOT EXISTS notifications.notification_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    template_id VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    type VARCHAR(20) NOT NULL,
    category VARCHAR(20) NOT NULL,
    language VARCHAR(5) NOT NULL,
    subject_template VARCHAR(500),
    content_template TEXT NOT NULL,
    html_template TEXT,
    template_variables JSON,
    active BOOLEAN NOT NULL DEFAULT true,
    version INTEGER NOT NULL DEFAULT 1,
    created_by BIGINT,
    updated_by BIGINT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    metadata JSON
);

CREATE INDEX IF NOT EXISTS idx_template_type ON notifications.notification_templates(type);
CREATE INDEX IF NOT EXISTS idx_template_category ON notifications.notification_templates(category);
CREATE INDEX IF NOT EXISTS idx_template_language ON notifications.notification_templates(language);
CREATE INDEX IF NOT EXISTS idx_template_active ON notifications.notification_templates(active);

-- ============================================================================
-- Table: notification_queue
-- Table for managing notification queue and retries
-- ============================================================================
CREATE TABLE IF NOT EXISTS notifications.notification_queue (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notification_id VARCHAR(64) NOT NULL,
    user_id UUID NOT NULL,
    priority VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL,
    channel_type VARCHAR(20) NOT NULL,
    retry_count INTEGER DEFAULT 0,
    max_retries INTEGER DEFAULT 3,
    next_retry_at TIMESTAMP WITH TIME ZONE,
    error_message TEXT,
    scheduled_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    processed_at TIMESTAMP WITH TIME ZONE,
    payload TEXT
);

CREATE INDEX IF NOT EXISTS idx_queue_status ON notifications.notification_queue(status);
CREATE INDEX IF NOT EXISTS idx_queue_priority ON notifications.notification_queue(priority);
CREATE INDEX IF NOT EXISTS idx_queue_scheduled_at ON notifications.notification_queue(scheduled_at);

-- ============================================================================
-- Table: user_notification_preferences
-- Table for storing user notification preferences
-- ============================================================================
CREATE TABLE IF NOT EXISTS notifications.user_notification_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE,
    email_enabled BOOLEAN NOT NULL DEFAULT true,
    push_enabled BOOLEAN NOT NULL DEFAULT true,
    in_app_enabled BOOLEAN NOT NULL DEFAULT true,
    telegram_enabled BOOLEAN NOT NULL DEFAULT false,
    sms_enabled BOOLEAN NOT NULL DEFAULT false,
    welcome_enabled BOOLEAN NOT NULL DEFAULT true,
    quest_enabled BOOLEAN NOT NULL DEFAULT true,
    game_enabled BOOLEAN NOT NULL DEFAULT true,
    team_enabled BOOLEAN NOT NULL DEFAULT true,
    system_enabled BOOLEAN NOT NULL DEFAULT true,
    security_enabled BOOLEAN NOT NULL DEFAULT true,
    marketing_enabled BOOLEAN NOT NULL DEFAULT false,
    reminder_enabled BOOLEAN NOT NULL DEFAULT true,
    do_not_disturb_enabled BOOLEAN NOT NULL DEFAULT false,
    do_not_disturb_start_hour INTEGER,
    do_not_disturb_end_hour INTEGER,
    max_notifications_per_hour INTEGER,
    max_notifications_per_day INTEGER,
    preferred_language VARCHAR(5) DEFAULT 'ru',
    time_zone VARCHAR(50) DEFAULT 'Europe/Moscow',
    email VARCHAR(255),
    phone VARCHAR(20),
    telegram_chat_id VARCHAR(50),
    fcm_token VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_by UUID,
    additional_settings JSON
);

CREATE INDEX IF NOT EXISTS idx_preferences_user_id ON notifications.user_notification_preferences(user_id);