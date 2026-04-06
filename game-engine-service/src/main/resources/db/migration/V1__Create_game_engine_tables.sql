-- Создание таблицы игровых сессий
CREATE TABLE game_sessions (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'CREATED',
    owner_id UUID NOT NULL,
    quest_id UUID NOT NULL,
    team_id UUID,
    current_level_id UUID,
    max_participants INTEGER DEFAULT 10,
    is_private BOOLEAN DEFAULT FALSE,
    requires_approval BOOLEAN DEFAULT FALSE,
    scheduled_start_time TIMESTAMP WITH TIME ZONE,
    duration_minutes INTEGER,
    total_score DECIMAL(10,2) DEFAULT 0.0,
    duration_seconds BIGINT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP WITH TIME ZONE,
    finished_at TIMESTAMP WITH TIME ZONE,
    last_activity_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    settings JSONB,
    metadata JSONB,
    
    CONSTRAINT fk_game_sessions_owner FOREIGN KEY (owner_id) REFERENCES users(id),
    CONSTRAINT fk_game_sessions_quest FOREIGN KEY (quest_id) REFERENCES quests(id),
    CONSTRAINT fk_game_sessions_team FOREIGN KEY (team_id) REFERENCES teams(id),
    CONSTRAINT fk_game_sessions_current_level FOREIGN KEY (current_level_id) REFERENCES levels(id),
    CONSTRAINT chk_game_sessions_status CHECK (status IN ('CREATED', 'ACTIVE', 'PAUSED', 'COMPLETED', 'CANCELLED', 'ARCHIVED')),
    CONSTRAINT chk_game_sessions_max_participants CHECK (max_participants > 0 AND max_participants <= 1000)
);

-- Создание индексов для game_sessions
CREATE INDEX idx_game_sessions_status ON game_sessions(status);
CREATE INDEX idx_game_sessions_owner_id ON game_sessions(owner_id);
CREATE INDEX idx_game_sessions_quest_id ON game_sessions(quest_id);
CREATE INDEX idx_game_sessions_team_id ON game_sessions(team_id);
CREATE INDEX idx_game_sessions_created_at ON game_sessions(created_at);
CREATE INDEX idx_game_sessions_last_activity_at ON game_sessions(last_activity_at);
CREATE INDEX idx_game_sessions_is_private ON game_sessions(is_private);
CREATE INDEX idx_game_sessions_requires_approval ON game_sessions(requires_approval);

-- Создание таблицы попыток ввода кодов
CREATE TABLE code_attempts (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL,
    user_id UUID NOT NULL,
    level_id UUID NOT NULL,
    submitted_code VARCHAR(255) NOT NULL,
    correct_code VARCHAR(255),
    sector VARCHAR(10) NOT NULL,
    result VARCHAR(50) NOT NULL,
    points DECIMAL(10,2) DEFAULT 0.0,
    bonus_points DECIMAL(10,2) DEFAULT 0.0,
    penalty_points DECIMAL(10,2) DEFAULT 0.0,
    total_score DECIMAL(10,2) DEFAULT 0.0,
    attempt_number INTEGER NOT NULL DEFAULT 1,
    time_spent_seconds DECIMAL(10,2),
    code_type VARCHAR(50) DEFAULT 'MAIN',
    is_bonus BOOLEAN DEFAULT FALSE,
    sector_multiplier DECIMAL(3,2) DEFAULT 1.0,
    difficulty_multiplier DECIMAL(3,2) DEFAULT 1.0,
    ip_address INET,
    user_agent TEXT,
    coordinate_x DECIMAL(10,6),
    coordinate_y DECIMAL(10,6),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    metadata JSONB,
    
    CONSTRAINT fk_code_attempts_session FOREIGN KEY (session_id) REFERENCES game_sessions(id) ON DELETE CASCADE,
    CONSTRAINT fk_code_attempts_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_code_attempts_level FOREIGN KEY (level_id) REFERENCES levels(id),
    CONSTRAINT chk_code_attempts_result CHECK (result IN ('CORRECT', 'INCORRECT', 'PARTIALLY_CORRECT', 'TIMEOUT', 'INVALID')),
    CONSTRAINT chk_code_attempts_sector CHECK (sector ~ '^[A-Za-z]$'),
    CONSTRAINT chk_code_attempts_attempt_number CHECK (attempt_number > 0)
);

-- Создание индексов для code_attempts
CREATE INDEX idx_code_attempts_session_id ON code_attempts(session_id);
CREATE INDEX idx_code_attempts_user_id ON code_attempts(user_id);
CREATE INDEX idx_code_attempts_level_id ON code_attempts(level_id);
CREATE INDEX idx_code_attempts_result ON code_attempts(result);
CREATE INDEX idx_code_attempts_sector ON code_attempts(sector);
CREATE INDEX idx_code_attempts_created_at ON code_attempts(created_at);
CREATE INDEX idx_code_attempts_submitted_code ON code_attempts(submitted_code);
CREATE INDEX idx_code_attempts_is_bonus ON code_attempts(is_bonus);

-- Создание таблицы прогресса по уровням
CREATE TABLE level_progress (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL,
    user_id UUID NOT NULL,
    level_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'NOT_STARTED',
    progress_percentage DECIMAL(5,2) DEFAULT 0.0,
    score DECIMAL(10,2) DEFAULT 0.0,
    bonus_points DECIMAL(10,2) DEFAULT 0.0,
    penalty_points DECIMAL(10,2) DEFAULT 0.0,
    total_score DECIMAL(10,2) DEFAULT 0.0,
    codes_found INTEGER DEFAULT 0,
    total_codes INTEGER DEFAULT 0,
    attempts_count INTEGER DEFAULT 0,
    correct_attempts INTEGER DEFAULT 0,
    incorrect_attempts INTEGER DEFAULT 0,
    hints_used INTEGER DEFAULT 0,
    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    last_activity_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    time_spent_seconds BIGINT DEFAULT 0,
    metadata JSONB,
    
    CONSTRAINT fk_level_progress_session FOREIGN KEY (session_id) REFERENCES game_sessions(id) ON DELETE CASCADE,
    CONSTRAINT fk_level_progress_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_level_progress_level FOREIGN KEY (level_id) REFERENCES levels(id),
    CONSTRAINT chk_level_progress_status CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED', 'SKIPPED', 'FAILED')),
    CONSTRAINT chk_level_progress_progress_percentage CHECK (progress_percentage >= 0.0 AND progress_percentage <= 100.0),
    CONSTRAINT chk_level_progress_codes_found CHECK (codes_found >= 0),
    CONSTRAINT chk_level_progress_total_codes CHECK (total_codes >= 0),
    CONSTRAINT chk_level_progress_attempts_count CHECK (attempts_count >= 0),
    CONSTRAINT uq_level_progress_session_user_level UNIQUE(session_id, user_id, level_id)
);

-- Создание индексов для level_progress
CREATE INDEX idx_level_progress_session_id ON level_progress(session_id);
CREATE INDEX idx_level_progress_user_id ON level_progress(user_id);
CREATE INDEX idx_level_progress_level_id ON level_progress(level_id);
CREATE INDEX idx_level_progress_status ON level_progress(status);
CREATE INDEX idx_level_progress_progress_percentage ON level_progress(progress_percentage);
CREATE INDEX idx_level_progress_started_at ON level_progress(started_at);
CREATE INDEX idx_level_progress_completed_at ON level_progress(completed_at);
CREATE INDEX idx_level_progress_last_activity_at ON level_progress(last_activity_at);

-- Создание таблицы завершенных уровней
CREATE TABLE level_completions (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL,
    user_id UUID NOT NULL,
    level_id UUID NOT NULL,
    completion_code VARCHAR(255) NOT NULL,
    completion_sector VARCHAR(10) NOT NULL,
    time_spent_seconds BIGINT NOT NULL,
    score DECIMAL(10,2) NOT NULL DEFAULT 0.0,
    bonus_points DECIMAL(10,2) DEFAULT 0.0,
    penalty_points DECIMAL(10,2) DEFAULT 0.0,
    final_score DECIMAL(10,2) NOT NULL DEFAULT 0.0,
    attempts_count INTEGER NOT NULL DEFAULT 1,
    hints_used INTEGER DEFAULT 0,
    efficiency_score DECIMAL(10,2),
    is_first_completion BOOLEAN DEFAULT FALSE,
    is_record_completion BOOLEAN DEFAULT FALSE,
    completion_rank INTEGER,
    completed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    metadata JSONB,
    
    CONSTRAINT fk_level_completions_session FOREIGN KEY (session_id) REFERENCES game_sessions(id) ON DELETE CASCADE,
    CONSTRAINT fk_level_completions_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_level_completions_level FOREIGN KEY (level_id) REFERENCES levels(id),
    CONSTRAINT chk_level_completions_time_spent CHECK (time_spent_seconds >= 0),
    CONSTRAINT chk_level_completions_attempts_count CHECK (attempts_count > 0),
    CONSTRAINT chk_level_completions_completion_rank CHECK (completion_rank > 0),
    CONSTRAINT uq_level_completions_session_user_level UNIQUE(session_id, user_id, level_id)
);

-- Создание индексов для level_completions
CREATE INDEX idx_level_completions_session_id ON level_completions(session_id);
CREATE INDEX idx_level_completions_user_id ON level_completions(user_id);
CREATE INDEX idx_level_completions_level_id ON level_completions(level_id);
CREATE INDEX idx_level_completions_final_score ON level_completions(final_score);
CREATE INDEX idx_level_completions_time_spent_seconds ON level_completions(time_spent_seconds);
CREATE INDEX idx_level_completions_completed_at ON level_completions(completed_at);
CREATE INDEX idx_level_completions_is_first_completion ON level_completions(is_first_completion);
CREATE INDEX idx_level_completions_is_record_completion ON level_completions(is_record_completion);
CREATE INDEX idx_level_completions_completion_rank ON level_completions(completion_rank);

-- Создание таблицы запросов на участие
CREATE TABLE participation_requests (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL,
    user_id UUID NOT NULL,
    team_id UUID,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    message TEXT,
    request_type VARCHAR(50) DEFAULT 'INDIVIDUAL',
    priority INTEGER DEFAULT 0,
    submitted_by UUID NOT NULL,
    approved_by UUID,
    rejected_by UUID,
    approved_at TIMESTAMP WITH TIME ZONE,
    rejected_at TIMESTAMP WITH TIME ZONE,
    cancelled_at TIMESTAMP WITH TIME ZONE,
    withdrawn_at TIMESTAMP WITH TIME ZONE,
    expires_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    metadata JSONB,
    
    CONSTRAINT fk_participation_requests_session FOREIGN KEY (session_id) REFERENCES game_sessions(id) ON DELETE CASCADE,
    CONSTRAINT fk_participation_requests_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_participation_requests_team FOREIGN KEY (team_id) REFERENCES teams(id),
    CONSTRAINT fk_participation_requests_submitted_by FOREIGN KEY (submitted_by) REFERENCES users(id),
    CONSTRAINT fk_participation_requests_approved_by FOREIGN KEY (approved_by) REFERENCES users(id),
    CONSTRAINT fk_participation_requests_rejected_by FOREIGN KEY (rejected_by) REFERENCES users(id),
    CONSTRAINT chk_participation_requests_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED', 'WITHDRAWN', 'EXPIRED')),
    CONSTRAINT chk_participation_requests_request_type CHECK (request_type IN ('INDIVIDUAL', 'TEAM', 'INVITATION')),
    CONSTRAINT chk_participation_requests_priority CHECK (priority >= 0),
    CONSTRAINT uq_participation_requests_session_user UNIQUE(session_id, user_id, status)
);

-- Создание индексов для participation_requests
CREATE INDEX idx_participation_requests_session_id ON participation_requests(session_id);
CREATE INDEX idx_participation_requests_user_id ON participation_requests(user_id);
CREATE INDEX idx_participation_requests_team_id ON participation_requests(team_id);
CREATE INDEX idx_participation_requests_status ON participation_requests(status);
CREATE INDEX idx_participation_requests_request_type ON participation_requests(request_type);
CREATE INDEX idx_participation_requests_priority ON participation_requests(priority);
CREATE INDEX idx_participation_requests_created_at ON participation_requests(created_at);
CREATE INDEX idx_participation_requests_expires_at ON participation_requests(expires_at);
CREATE INDEX idx_participation_requests_submitted_by ON participation_requests(submitted_by);

-- Создание таблицы участников сессий (для связи многие-ко-многим)
CREATE TABLE session_participants (
    session_id UUID NOT NULL,
    user_id UUID NOT NULL,
    joined_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    left_at TIMESTAMP WITH TIME ZONE,
    is_active BOOLEAN DEFAULT TRUE,
    role VARCHAR(50) DEFAULT 'PARTICIPANT',
    metadata JSONB,
    
    CONSTRAINT fk_session_participants_session FOREIGN KEY (session_id) REFERENCES game_sessions(id) ON DELETE CASCADE,
    CONSTRAINT fk_session_participants_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_session_participants_role CHECK (role IN ('PARTICIPANT', 'OBSERVER', 'MODERATOR')),
    CONSTRAINT pk_session_participants PRIMARY KEY (session_id, user_id)
);

-- Создание индексов для session_participants
CREATE INDEX idx_session_participants_user_id ON session_participants(user_id);
CREATE INDEX idx_session_participants_joined_at ON session_participants(joined_at);
CREATE INDEX idx_session_participants_is_active ON session_participants(is_active);
CREATE INDEX idx_session_participants_role ON session_participants(role);

-- Добавление комментариев к таблицам
COMMENT ON TABLE game_sessions IS 'Игровые сессии';
COMMENT ON TABLE code_attempts IS 'Попытки ввода кодов';
COMMENT ON TABLE level_progress IS 'Прогресс по уровням';
COMMENT ON TABLE level_completions IS 'Завершенные уровни';
COMMENT ON TABLE participation_requests IS 'Запросы на участие';
COMMENT ON TABLE session_participants IS 'Участники сессий';

-- Создание триггера для обновления updated_at в participation_requests
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_participation_requests_updated_at 
    BEFORE UPDATE ON participation_requests 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();