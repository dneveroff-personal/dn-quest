-- =============================================
-- game-engine-service — V1__init.sql
-- Schema: game
-- =============================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ language 'plpgsql';

-- =============================================
-- Локальные копии справочных сущностей
-- =============================================
CREATE TABLE IF NOT EXISTS users (
                                     id               UUID PRIMARY KEY,
                                     username         VARCHAR(100) UNIQUE NOT NULL,
    email            VARCHAR(255) UNIQUE NOT NULL,
    password_hash    VARCHAR(255) NOT NULL,
    first_name       VARCHAR(100),
    last_name        VARCHAR(100),
    display_name     VARCHAR(255),
    avatar_url       VARCHAR(500),
    bio              TEXT,
    rating           DECIMAL(10,2) DEFAULT 1000.0,
    level            INTEGER       DEFAULT 1,
    experience_points BIGINT       DEFAULT 0,
    is_active        BOOLEAN       DEFAULT TRUE,
    is_verified      BOOLEAN       DEFAULT FALSE,
    last_login_at    TIMESTAMP WITH TIME ZONE,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   settings         JSONB,
                                   preferences      JSONB,
                                   metadata         JSONB,

                                   CONSTRAINT chk_users_rating           CHECK (rating >= 0),
    CONSTRAINT chk_users_level            CHECK (level > 0),
    CONSTRAINT chk_users_experience_points CHECK (experience_points >= 0)
    );

CREATE INDEX IF NOT EXISTS idx_users_username  ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email     ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_rating    ON users(rating);
CREATE INDEX IF NOT EXISTS idx_users_level     ON users(level);
CREATE INDEX IF NOT EXISTS idx_users_is_active ON users(is_active);

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TABLE IF NOT EXISTS teams (
                                     id                   UUID PRIMARY KEY,
                                     name                 VARCHAR(255) UNIQUE NOT NULL,
    description          TEXT,
    captain_id           UUID NOT NULL,
    logo_url             VARCHAR(500),
    rating               DECIMAL(10,2) DEFAULT 1000.0,
    total_games_played   INTEGER       DEFAULT 0,
    total_games_won      INTEGER       DEFAULT 0,
    total_playtime_seconds BIGINT      DEFAULT 0,
    max_members          INTEGER       DEFAULT 10,
    is_active            BOOLEAN       DEFAULT TRUE,
    is_public            BOOLEAN       DEFAULT TRUE,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       settings             JSONB,
                                       metadata             JSONB,

                                       CONSTRAINT fk_teams_captain               FOREIGN KEY (captain_id) REFERENCES users(id),
    CONSTRAINT chk_teams_rating               CHECK (rating >= 0),
    CONSTRAINT chk_teams_total_games_played   CHECK (total_games_played >= 0),
    CONSTRAINT chk_teams_total_games_won      CHECK (total_games_won >= 0 AND total_games_won <= total_games_played),
    CONSTRAINT chk_teams_max_members          CHECK (max_members > 0 AND max_members <= 100)
    );

CREATE INDEX IF NOT EXISTS idx_teams_captain_id ON teams(captain_id);
CREATE INDEX IF NOT EXISTS idx_teams_rating     ON teams(rating);
CREATE INDEX IF NOT EXISTS idx_teams_is_active  ON teams(is_active);

CREATE TRIGGER trg_teams_updated_at
    BEFORE UPDATE ON teams
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TABLE IF NOT EXISTS team_members (
                                            id                    UUID PRIMARY KEY,
                                            team_id               UUID NOT NULL,
                                            user_id               UUID NOT NULL,
                                            role                  VARCHAR(50) NOT NULL DEFAULT 'MEMBER',
    joined_at             TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    left_at               TIMESTAMP WITH TIME ZONE,
                                        is_active             BOOLEAN DEFAULT TRUE,
                                        games_played          INTEGER DEFAULT 0,
                                        games_won             INTEGER DEFAULT 0,
                                        total_playtime_seconds BIGINT DEFAULT 0,
                                        rating                DECIMAL(10,2) DEFAULT 1000.0,
    metadata              JSONB,

    CONSTRAINT fk_team_members_team FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
    CONSTRAINT fk_team_members_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_team_members_role CHECK (role IN ('CAPTAIN', 'MEMBER', 'MODERATOR')),
    CONSTRAINT uq_team_members_team_user UNIQUE(team_id, user_id)
    );

CREATE INDEX IF NOT EXISTS idx_team_members_team_id  ON team_members(team_id);
CREATE INDEX IF NOT EXISTS idx_team_members_user_id  ON team_members(user_id);
CREATE INDEX IF NOT EXISTS idx_team_members_is_active ON team_members(is_active);

CREATE TABLE IF NOT EXISTS quests (
                                      id                         UUID PRIMARY KEY,
                                      name                       VARCHAR(255) NOT NULL,
    description                TEXT,
    type                       VARCHAR(50) NOT NULL DEFAULT 'ADVENTURE',
    difficulty                 VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
    author_id                  UUID NOT NULL,
    is_published               BOOLEAN DEFAULT FALSE,
    is_archived                BOOLEAN DEFAULT FALSE,
    rating                     DECIMAL(3,2) DEFAULT 0.0,
    total_ratings              INTEGER DEFAULT 0,
    estimated_duration_minutes INTEGER,
    max_participants           INTEGER DEFAULT 10,
    min_participants           INTEGER DEFAULT 1,
    level_ids                  UUID[],
    tags                       TEXT[],
    settings                   JSONB,
    metadata                   JSONB,
    created_at                 TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                 TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at               TIMESTAMP WITH TIME ZONE,

                                             CONSTRAINT fk_quests_author         FOREIGN KEY (author_id) REFERENCES users(id),
    CONSTRAINT chk_quests_type          CHECK (type IN ('ADVENTURE', 'MYSTERY', 'PUZZLE', 'ESCAPE', 'EDUCATIONAL', 'COMPETITIVE')),
    CONSTRAINT chk_quests_difficulty    CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD', 'EXPERT')),
    CONSTRAINT chk_quests_rating        CHECK (rating >= 0.0 AND rating <= 5.0),
    CONSTRAINT chk_quests_max_participants CHECK (max_participants > 0 AND max_participants <= 1000),
    CONSTRAINT chk_quests_min_participants CHECK (min_participants > 0 AND min_participants <= max_participants)
    );

CREATE INDEX IF NOT EXISTS idx_quests_author_id   ON quests(author_id);
CREATE INDEX IF NOT EXISTS idx_quests_type        ON quests(type);
CREATE INDEX IF NOT EXISTS idx_quests_difficulty  ON quests(difficulty);
CREATE INDEX IF NOT EXISTS idx_quests_is_published ON quests(is_published);

CREATE TRIGGER trg_quests_updated_at
    BEFORE UPDATE ON quests
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TABLE IF NOT EXISTS levels (
                                      id                      UUID PRIMARY KEY,
                                      quest_id                UUID NOT NULL,
                                      name                    VARCHAR(255) NOT NULL,
    description             TEXT,
    order_number            INTEGER NOT NULL,
    difficulty              VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
    estimated_time_minutes  INTEGER,
    max_attempts            INTEGER DEFAULT 0,
    hint_penalty_points     DECIMAL(10,2) DEFAULT 10.0,
    time_limit_minutes      INTEGER,
    is_required             BOOLEAN DEFAULT TRUE,
    is_bonus                BOOLEAN DEFAULT FALSE,
    settings                JSONB,
    metadata                JSONB,
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                          CONSTRAINT fk_levels_quest        FOREIGN KEY (quest_id) REFERENCES quests(id) ON DELETE CASCADE,
    CONSTRAINT chk_levels_difficulty  CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD', 'EXPERT')),
    CONSTRAINT chk_levels_order_number CHECK (order_number > 0),
    CONSTRAINT uq_levels_quest_order  UNIQUE(quest_id, order_number)
    );

CREATE INDEX IF NOT EXISTS idx_levels_quest_id    ON levels(quest_id);
CREATE INDEX IF NOT EXISTS idx_levels_order_number ON levels(order_number);
CREATE INDEX IF NOT EXISTS idx_levels_is_required ON levels(is_required);

CREATE TRIGGER trg_levels_updated_at
    BEFORE UPDATE ON levels
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TABLE IF NOT EXISTS codes (
                                     id               UUID PRIMARY KEY,
                                     level_id         UUID         NOT NULL,
                                     code_value       VARCHAR(255) NOT NULL,
    sector           VARCHAR(10)  NOT NULL,
    code_type        VARCHAR(50)  NOT NULL DEFAULT 'MAIN',
    points           DECIMAL(10,2) NOT NULL DEFAULT 0.0,
    is_active        BOOLEAN DEFAULT TRUE,
    is_bonus         BOOLEAN DEFAULT FALSE,
    bonus_multiplier DECIMAL(3,2) DEFAULT 1.0,
    description      TEXT,
    hint             TEXT,
    metadata         JSONB,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                   CONSTRAINT fk_codes_level         FOREIGN KEY (level_id) REFERENCES levels(id) ON DELETE CASCADE,
    CONSTRAINT chk_codes_sector       CHECK (sector ~ '^[A-Za-z]$'),
    CONSTRAINT chk_codes_code_type    CHECK (code_type IN ('MAIN', 'BONUS', 'SECRET', 'TIME_BONUS', 'PENALTY')),
    CONSTRAINT chk_codes_points       CHECK (points >= 0),
    CONSTRAINT uq_codes_level_sector_type UNIQUE(level_id, sector, code_type)
    );

CREATE INDEX IF NOT EXISTS idx_codes_level_id  ON codes(level_id);
CREATE INDEX IF NOT EXISTS idx_codes_sector    ON codes(sector);
CREATE INDEX IF NOT EXISTS idx_codes_code_type ON codes(code_type);
CREATE INDEX IF NOT EXISTS idx_codes_is_active ON codes(is_active);

CREATE TRIGGER trg_codes_updated_at
    BEFORE UPDATE ON codes
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TABLE IF NOT EXISTS level_hints (
                                           id              UUID PRIMARY KEY,
                                           level_id        UUID NOT NULL,
                                           hint_text       TEXT NOT NULL,
                                           order_number    INTEGER NOT NULL,
                                           penalty_points  DECIMAL(10,2) DEFAULT 10.0,
    is_active       BOOLEAN DEFAULT TRUE,
    requires_code   VARCHAR(255),
    metadata        JSONB,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                  CONSTRAINT fk_level_hints_level       FOREIGN KEY (level_id) REFERENCES levels(id) ON DELETE CASCADE,
    CONSTRAINT chk_level_hints_order_number CHECK (order_number > 0),
    CONSTRAINT uq_level_hints_level_order  UNIQUE(level_id, order_number)
    );

CREATE INDEX IF NOT EXISTS idx_level_hints_level_id     ON level_hints(level_id);
CREATE INDEX IF NOT EXISTS idx_level_hints_order_number ON level_hints(order_number);
CREATE INDEX IF NOT EXISTS idx_level_hints_is_active    ON level_hints(is_active);

CREATE TRIGGER trg_level_hints_updated_at
    BEFORE UPDATE ON level_hints
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- Игровые сессии
-- =============================================
CREATE TABLE game_sessions (
                               id                    UUID PRIMARY KEY,
                               name                  VARCHAR(255) NOT NULL,
                               description           TEXT,
                               status                VARCHAR(50)  NOT NULL DEFAULT 'CREATED',
                               owner_id              UUID         NOT NULL,
                               quest_id              UUID         NOT NULL,
                               team_id               UUID,
                               current_level_id      UUID,
                               max_participants      INTEGER      DEFAULT 10,
                               is_private            BOOLEAN      DEFAULT FALSE,
                               requires_approval     BOOLEAN      DEFAULT FALSE,
                               scheduled_start_time  TIMESTAMP WITH TIME ZONE,
                               duration_minutes      INTEGER,
                               total_score           DECIMAL(10,2) DEFAULT 0.0,
                               duration_seconds      BIGINT,
                               settings              JSONB,
                               metadata              JSONB,
                               created_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               started_at            TIMESTAMP WITH TIME ZONE,
                               finished_at           TIMESTAMP WITH TIME ZONE,
                               last_activity_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT fk_game_sessions_owner         FOREIGN KEY (owner_id)         REFERENCES users(id),
                               CONSTRAINT fk_game_sessions_quest         FOREIGN KEY (quest_id)         REFERENCES quests(id),
                               CONSTRAINT fk_game_sessions_team          FOREIGN KEY (team_id)          REFERENCES teams(id),
                               CONSTRAINT fk_game_sessions_current_level FOREIGN KEY (current_level_id) REFERENCES levels(id),
                               CONSTRAINT chk_game_sessions_status       CHECK (status IN ('CREATED', 'ACTIVE', 'PAUSED', 'COMPLETED', 'CANCELLED', 'ARCHIVED')),
                               CONSTRAINT chk_game_sessions_max_participants CHECK (max_participants > 0 AND max_participants <= 1000)
);

CREATE INDEX idx_game_sessions_status          ON game_sessions(status);
CREATE INDEX idx_game_sessions_owner_id        ON game_sessions(owner_id);
CREATE INDEX idx_game_sessions_quest_id        ON game_sessions(quest_id);
CREATE INDEX idx_game_sessions_team_id         ON game_sessions(team_id);
CREATE INDEX idx_game_sessions_created_at      ON game_sessions(created_at);
CREATE INDEX idx_game_sessions_last_activity_at ON game_sessions(last_activity_at);

COMMENT ON TABLE game_sessions IS 'Игровые сессии';

-- =============================================
-- Участники сессий
-- =============================================
CREATE TABLE session_participants (
                                      session_id UUID NOT NULL,
                                      user_id    UUID NOT NULL,
                                      joined_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      left_at    TIMESTAMP WITH TIME ZONE,
                                      is_active  BOOLEAN DEFAULT TRUE,
                                      role       VARCHAR(50) DEFAULT 'PARTICIPANT',
                                      metadata   JSONB,

                                      CONSTRAINT pk_session_participants       PRIMARY KEY (session_id, user_id),
                                      CONSTRAINT fk_session_participants_session FOREIGN KEY (session_id) REFERENCES game_sessions(id) ON DELETE CASCADE,
                                      CONSTRAINT fk_session_participants_user    FOREIGN KEY (user_id)    REFERENCES users(id) ON DELETE CASCADE,
                                      CONSTRAINT chk_session_participants_role   CHECK (role IN ('PARTICIPANT', 'OBSERVER', 'MODERATOR'))
);

CREATE INDEX idx_session_participants_user_id  ON session_participants(user_id);
CREATE INDEX idx_session_participants_is_active ON session_participants(is_active);

-- =============================================
-- Попытки ввода кодов
-- =============================================
CREATE TABLE code_attempts (
                               id                    UUID PRIMARY KEY,
                               session_id            UUID         NOT NULL,
                               user_id               UUID         NOT NULL,
                               level_id              UUID         NOT NULL,
                               submitted_code        VARCHAR(255) NOT NULL,
                               correct_code          VARCHAR(255),
                               sector                VARCHAR(10)  NOT NULL,
                               result                VARCHAR(50)  NOT NULL,
                               points                DECIMAL(10,2) DEFAULT 0.0,
                               bonus_points          DECIMAL(10,2) DEFAULT 0.0,
                               penalty_points        DECIMAL(10,2) DEFAULT 0.0,
                               total_score           DECIMAL(10,2) DEFAULT 0.0,
                               attempt_number        INTEGER       NOT NULL DEFAULT 1,
                               time_spent_seconds    DECIMAL(10,2),
                               code_type             VARCHAR(50)   DEFAULT 'MAIN',
                               is_bonus              BOOLEAN       DEFAULT FALSE,
                               sector_multiplier     DECIMAL(3,2)  DEFAULT 1.0,
                               difficulty_multiplier DECIMAL(3,2)  DEFAULT 1.0,
                               ip_address            INET,
                               user_agent            TEXT,
                               coordinate_x          DECIMAL(10,6),
                               coordinate_y          DECIMAL(10,6),
                               metadata              JSONB,
                               created_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT fk_code_attempts_session FOREIGN KEY (session_id) REFERENCES game_sessions(id) ON DELETE CASCADE,
                               CONSTRAINT fk_code_attempts_user    FOREIGN KEY (user_id)    REFERENCES users(id),
                               CONSTRAINT fk_code_attempts_level   FOREIGN KEY (level_id)   REFERENCES levels(id),
                               CONSTRAINT chk_code_attempts_result CHECK (result IN ('CORRECT', 'INCORRECT', 'PARTIALLY_CORRECT', 'TIMEOUT', 'INVALID')),
                               CONSTRAINT chk_code_attempts_sector CHECK (sector ~ '^[A-Za-z]$'),
    CONSTRAINT chk_code_attempts_attempt_number CHECK (attempt_number > 0)
);

CREATE INDEX idx_code_attempts_session_id    ON code_attempts(session_id);
CREATE INDEX idx_code_attempts_user_id       ON code_attempts(user_id);
CREATE INDEX idx_code_attempts_level_id      ON code_attempts(level_id);
CREATE INDEX idx_code_attempts_result        ON code_attempts(result);
CREATE INDEX idx_code_attempts_created_at    ON code_attempts(created_at);

COMMENT ON TABLE code_attempts IS 'Попытки ввода кодов';

-- =============================================
-- Прогресс по уровням
-- =============================================
CREATE TABLE level_progress (
                                id                  UUID PRIMARY KEY,
                                session_id          UUID    NOT NULL,
                                user_id             UUID    NOT NULL,
                                level_id            UUID    NOT NULL,
                                status              VARCHAR(50)   NOT NULL DEFAULT 'NOT_STARTED',
                                progress_percentage DECIMAL(5,2)  DEFAULT 0.0,
                                score               DECIMAL(10,2) DEFAULT 0.0,
                                bonus_points        DECIMAL(10,2) DEFAULT 0.0,
                                penalty_points      DECIMAL(10,2) DEFAULT 0.0,
                                total_score         DECIMAL(10,2) DEFAULT 0.0,
                                codes_found         INTEGER       DEFAULT 0,
                                total_codes         INTEGER       DEFAULT 0,
                                attempts_count      INTEGER       DEFAULT 0,
                                correct_attempts    INTEGER       DEFAULT 0,
                                incorrect_attempts  INTEGER       DEFAULT 0,
                                hints_used          INTEGER       DEFAULT 0,
                                time_spent_seconds  BIGINT        DEFAULT 0,
                                metadata            JSONB,
                                started_at          TIMESTAMP WITH TIME ZONE,
                                completed_at        TIMESTAMP WITH TIME ZONE,
                                last_activity_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                CONSTRAINT fk_level_progress_session FOREIGN KEY (session_id) REFERENCES game_sessions(id) ON DELETE CASCADE,
                                CONSTRAINT fk_level_progress_user    FOREIGN KEY (user_id)    REFERENCES users(id),
                                CONSTRAINT fk_level_progress_level   FOREIGN KEY (level_id)   REFERENCES levels(id),
                                CONSTRAINT chk_level_progress_status CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED', 'SKIPPED', 'FAILED')),
                                CONSTRAINT chk_level_progress_percentage CHECK (progress_percentage >= 0.0 AND progress_percentage <= 100.0),
                                CONSTRAINT uq_level_progress_session_user_level UNIQUE(session_id, user_id, level_id)
);

CREATE INDEX idx_level_progress_session_id   ON level_progress(session_id);
CREATE INDEX idx_level_progress_user_id      ON level_progress(user_id);
CREATE INDEX idx_level_progress_level_id     ON level_progress(level_id);
CREATE INDEX idx_level_progress_status       ON level_progress(status);

COMMENT ON TABLE level_progress IS 'Прогресс по уровням';

-- =============================================
-- Завершённые уровни
-- =============================================
CREATE TABLE level_completions (
                                   id                    UUID PRIMARY KEY,
                                   session_id            UUID         NOT NULL,
                                   user_id               UUID         NOT NULL,
                                   level_id              UUID         NOT NULL,
                                   completion_code       VARCHAR(255) NOT NULL,
                                   completion_sector     VARCHAR(10)  NOT NULL,
                                   time_spent_seconds    BIGINT       NOT NULL,
                                   score                 DECIMAL(10,2) NOT NULL DEFAULT 0.0,
                                   bonus_points          DECIMAL(10,2) DEFAULT 0.0,
                                   penalty_points        DECIMAL(10,2) DEFAULT 0.0,
                                   final_score           DECIMAL(10,2) NOT NULL DEFAULT 0.0,
                                   attempts_count        INTEGER       NOT NULL DEFAULT 1,
                                   hints_used            INTEGER       DEFAULT 0,
                                   efficiency_score      DECIMAL(10,2),
                                   is_first_completion   BOOLEAN       DEFAULT FALSE,
                                   is_record_completion  BOOLEAN       DEFAULT FALSE,
                                   completion_rank       INTEGER,
                                   metadata              JSONB,
                                   completed_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                   CONSTRAINT fk_level_completions_session FOREIGN KEY (session_id) REFERENCES game_sessions(id) ON DELETE CASCADE,
                                   CONSTRAINT fk_level_completions_user    FOREIGN KEY (user_id)    REFERENCES users(id),
                                   CONSTRAINT fk_level_completions_level   FOREIGN KEY (level_id)   REFERENCES levels(id),
                                   CONSTRAINT chk_level_completions_time_spent    CHECK (time_spent_seconds >= 0),
                                   CONSTRAINT chk_level_completions_attempts      CHECK (attempts_count > 0),
                                   CONSTRAINT uq_level_completions_session_user_level UNIQUE(session_id, user_id, level_id)
);

CREATE INDEX idx_level_completions_session_id  ON level_completions(session_id);
CREATE INDEX idx_level_completions_user_id     ON level_completions(user_id);
CREATE INDEX idx_level_completions_level_id    ON level_completions(level_id);
CREATE INDEX idx_level_completions_final_score ON level_completions(final_score);
CREATE INDEX idx_level_completions_completed_at ON level_completions(completed_at);

COMMENT ON TABLE level_completions IS 'Завершённые уровни';

-- =============================================
-- Запросы на участие
-- =============================================
CREATE TABLE participation_requests (
                                        id           UUID PRIMARY KEY,
                                        session_id   UUID    NOT NULL,
                                        user_id      UUID    NOT NULL,
                                        team_id      UUID,
                                        status       VARCHAR(50) NOT NULL DEFAULT 'PENDING',
                                        message      TEXT,
                                        request_type VARCHAR(50)  DEFAULT 'INDIVIDUAL',
                                        priority     INTEGER      DEFAULT 0,
                                        submitted_by UUID         NOT NULL,
                                        approved_by  UUID,
                                        rejected_by  UUID,
                                        approved_at  TIMESTAMP WITH TIME ZONE,
                                        rejected_at  TIMESTAMP WITH TIME ZONE,
                                        cancelled_at TIMESTAMP WITH TIME ZONE,
                                        withdrawn_at TIMESTAMP WITH TIME ZONE,
                                        expires_at   TIMESTAMP WITH TIME ZONE,
                                        metadata     JSONB,
                                        created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        updated_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                        CONSTRAINT fk_participation_requests_session      FOREIGN KEY (session_id)   REFERENCES game_sessions(id) ON DELETE CASCADE,
                                        CONSTRAINT fk_participation_requests_user         FOREIGN KEY (user_id)      REFERENCES users(id),
                                        CONSTRAINT fk_participation_requests_team         FOREIGN KEY (team_id)      REFERENCES teams(id),
                                        CONSTRAINT fk_participation_requests_submitted_by FOREIGN KEY (submitted_by) REFERENCES users(id),
                                        CONSTRAINT fk_participation_requests_approved_by  FOREIGN KEY (approved_by)  REFERENCES users(id),
                                        CONSTRAINT fk_participation_requests_rejected_by  FOREIGN KEY (rejected_by)  REFERENCES users(id),
                                        CONSTRAINT chk_participation_requests_status       CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED', 'WITHDRAWN', 'EXPIRED')),
                                        CONSTRAINT chk_participation_requests_request_type CHECK (request_type IN ('INDIVIDUAL', 'TEAM', 'INVITATION')),
                                        CONSTRAINT uq_participation_requests_session_user  UNIQUE(session_id, user_id, status)
);

CREATE INDEX idx_participation_requests_session_id ON participation_requests(session_id);
CREATE INDEX idx_participation_requests_user_id    ON participation_requests(user_id);
CREATE INDEX idx_participation_requests_status     ON participation_requests(status);
CREATE INDEX idx_participation_requests_expires_at ON participation_requests(expires_at);

CREATE TRIGGER trg_participation_requests_updated_at
    BEFORE UPDATE ON participation_requests
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE participation_requests IS 'Запросы на участие';