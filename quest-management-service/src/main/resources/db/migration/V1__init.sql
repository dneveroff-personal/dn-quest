-- =============================================
-- quest-management-service — V1__init.sql
-- Schema: quests
-- =============================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ language 'plpgsql';

-- =============================================
-- Таблица квестов
-- =============================================
CREATE TABLE quests (
                        id               UUID PRIMARY KEY,
                        number           BIGINT       UNIQUE NOT NULL,
                        title            VARCHAR(255) NOT NULL,
                        description      TEXT,
                        difficulty       VARCHAR(50),
                        quest_type       VARCHAR(50),
                        category         VARCHAR(100),
                        estimated_duration INTEGER,
                        max_participants INTEGER,
                        min_participants INTEGER,
                        start_location   VARCHAR(255),
                        end_location     VARCHAR(255),
                        rules            TEXT,
                        prizes           TEXT,
                        requirements     TEXT,
                        tags             TEXT[],
                        is_public        BOOLEAN DEFAULT false,
                        is_template      BOOLEAN DEFAULT false,
                        author_ids       UUID[],
                        status           VARCHAR(50)  DEFAULT 'DRAFT',
                        version          INTEGER      DEFAULT 1,
                        start_time       TIMESTAMP,
                        end_time         TIMESTAMP,
                        published_at     TIMESTAMP,
                        archived_at      TIMESTAMP,
                        archive_reason   TEXT,
                        created_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                        updated_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_quests_number       ON quests(number);
CREATE INDEX idx_quests_status       ON quests(status);
CREATE INDEX idx_quests_difficulty   ON quests(difficulty);
CREATE INDEX idx_quests_quest_type   ON quests(quest_type);
CREATE INDEX idx_quests_category     ON quests(category);
CREATE INDEX idx_quests_is_public    ON quests(is_public);
CREATE INDEX idx_quests_is_template  ON quests(is_template);
CREATE INDEX idx_quests_created_at   ON quests(created_at);
CREATE INDEX idx_quests_published_at ON quests(published_at);
CREATE INDEX idx_quests_author_ids   ON quests USING GIN(author_ids);
CREATE INDEX idx_quests_tags         ON quests USING GIN(tags);

COMMENT ON TABLE  quests                    IS 'Таблица квестов';
COMMENT ON COLUMN quests.id                 IS 'Уникальный идентификатор квеста';
COMMENT ON COLUMN quests.number             IS 'Уникальный номер квеста';
COMMENT ON COLUMN quests.title              IS 'Название квеста';
COMMENT ON COLUMN quests.estimated_duration IS 'Предполагаемая длительность в минутах';
COMMENT ON COLUMN quests.tags               IS 'Теги квеста';
COMMENT ON COLUMN quests.author_ids         IS 'Массив ID авторов квеста';
COMMENT ON COLUMN quests.status             IS 'Статус квеста (DRAFT, PUBLISHED, ACTIVE, ARCHIVED)';

CREATE TRIGGER trg_quests_updated_at
    BEFORE UPDATE ON quests
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- Таблица уровней квестов
-- =============================================
CREATE TABLE levels (
                        id         UUID PRIMARY KEY,
                        quest_id   UUID         NOT NULL,
                        title      VARCHAR(255) NOT NULL,
                        description TEXT,
                        "order"    INTEGER      NOT NULL,
                        location   VARCHAR(255),
                        latitude   DECIMAL(10, 8),
                        longitude  DECIMAL(11, 8),
                        radius     DECIMAL(8, 2),
                        media_ids  UUID[],
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                        CONSTRAINT fk_levels_quest     FOREIGN KEY (quest_id) REFERENCES quests(id) ON DELETE CASCADE,
                        CONSTRAINT uq_levels_quest_order UNIQUE (quest_id, "order")
);

CREATE INDEX idx_levels_quest_id  ON levels(quest_id);
CREATE INDEX idx_levels_order     ON levels("order");
CREATE INDEX idx_levels_location  ON levels(latitude, longitude);
CREATE INDEX idx_levels_media_ids ON levels USING GIN(media_ids);

COMMENT ON TABLE  levels             IS 'Таблица уровней квестов';
COMMENT ON COLUMN levels.id          IS 'Уникальный идентификатор уровня';
COMMENT ON COLUMN levels.quest_id    IS 'ID квеста';
COMMENT ON COLUMN levels."order"     IS 'Порядковый номер уровня в квесте';
COMMENT ON COLUMN levels.radius      IS 'Радиус в метрах';
COMMENT ON COLUMN levels.media_ids   IS 'Массив ID медиа файлов';

CREATE TRIGGER trg_levels_updated_at
    BEFORE UPDATE ON levels
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- Таблица кодов уровней
-- =============================================
CREATE TABLE codes (
                       id          UUID PRIMARY KEY,
                       level_id    UUID         NOT NULL,
                       value       VARCHAR(255) NOT NULL,
                       type        VARCHAR(50)  NOT NULL,
                       hint        TEXT,
                       points      INTEGER      DEFAULT 0,
                       active      BOOLEAN      DEFAULT true,
                       usage_limit INTEGER,
                       usage_count INTEGER      DEFAULT 0,
                       used_by     UUID[],
                       created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                       updated_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT fk_codes_level       FOREIGN KEY (level_id) REFERENCES levels(id) ON DELETE CASCADE,
                       CONSTRAINT uq_codes_level_value UNIQUE (level_id, value)
);

CREATE INDEX idx_codes_level_id  ON codes(level_id);
CREATE INDEX idx_codes_value     ON codes(value);
CREATE INDEX idx_codes_type      ON codes(type);
CREATE INDEX idx_codes_active    ON codes(active);
CREATE INDEX idx_codes_used_by   ON codes USING GIN(used_by);

COMMENT ON TABLE  codes             IS 'Таблица кодов уровней';
COMMENT ON COLUMN codes.usage_limit IS 'Лимит использований (null — без лимита)';
COMMENT ON COLUMN codes.used_by     IS 'Массив ID пользователей, использовавших код';

CREATE TRIGGER trg_codes_updated_at
    BEFORE UPDATE ON codes
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- Таблица подсказок уровней
-- =============================================
CREATE TABLE level_hints (
                             id              UUID PRIMARY KEY,
                             level_id        UUID    NOT NULL,
                             text            TEXT    NOT NULL,
                             cost            INTEGER DEFAULT 0,
                             available_after TIMESTAMP,
                             active          BOOLEAN DEFAULT true,
                             used_by         UUID[],
                             created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                             CONSTRAINT fk_level_hints_level FOREIGN KEY (level_id) REFERENCES levels(id) ON DELETE CASCADE
);

CREATE INDEX idx_level_hints_level_id        ON level_hints(level_id);
CREATE INDEX idx_level_hints_cost            ON level_hints(cost);
CREATE INDEX idx_level_hints_available_after ON level_hints(available_after);
CREATE INDEX idx_level_hints_active          ON level_hints(active);

COMMENT ON TABLE  level_hints                  IS 'Таблица подсказок уровней';
COMMENT ON COLUMN level_hints.cost             IS 'Стоимость подсказки в очках';
COMMENT ON COLUMN level_hints.available_after  IS 'Время, после которого подсказка доступна';

CREATE TRIGGER trg_level_hints_updated_at
    BEFORE UPDATE ON level_hints
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- Таблица медиа файлов квестов
-- =============================================
CREATE TABLE quest_media (
                             id                 UUID PRIMARY KEY,
                             quest_id           UUID         NOT NULL,
                             level_id           UUID,
                             file_name          VARCHAR(255) NOT NULL,
                             original_file_name VARCHAR(255) NOT NULL,
                             file_path          VARCHAR(500) NOT NULL,
                             file_size          BIGINT       NOT NULL,
                             mime_type          VARCHAR(100) NOT NULL,
                             media_type         VARCHAR(50)  NOT NULL,
                             description        TEXT,
                             display_order      INTEGER      DEFAULT 0,
                             is_public          BOOLEAN      DEFAULT true,
                             created_at         TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
                             updated_at         TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,

                             CONSTRAINT fk_quest_media_quest FOREIGN KEY (quest_id) REFERENCES quests(id) ON DELETE CASCADE,
                             CONSTRAINT fk_quest_media_level FOREIGN KEY (level_id) REFERENCES levels(id) ON DELETE CASCADE
);

CREATE INDEX idx_quest_media_quest_id      ON quest_media(quest_id);
CREATE INDEX idx_quest_media_level_id      ON quest_media(level_id);
CREATE INDEX idx_quest_media_media_type    ON quest_media(media_type);
CREATE INDEX idx_quest_media_is_public     ON quest_media(is_public);
CREATE INDEX idx_quest_media_display_order ON quest_media(display_order);

COMMENT ON TABLE  quest_media            IS 'Таблица медиа файлов квестов';
COMMENT ON COLUMN quest_media.media_type IS 'Тип медиа (IMAGE, VIDEO, AUDIO, DOCUMENT)';
COMMENT ON COLUMN quest_media.level_id   IS 'ID уровня (null — медиа относится ко всему квесту)';

CREATE TRIGGER trg_quest_media_updated_at
    BEFORE UPDATE ON quest_media
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- Таблица отзывов на квесты
-- =============================================
CREATE TABLE quest_reviews (
                               id         UUID PRIMARY KEY,
                               quest_id   UUID         NOT NULL,
                               user_id    UUID         NOT NULL,
                               title      VARCHAR(200),
                               content    TEXT,
                               rating     INTEGER CHECK (rating >= 1 AND rating <= 5),
                               is_visible BOOLEAN      NOT NULL DEFAULT TRUE,
                               created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP,

                               CONSTRAINT fk_quest_reviews_quest  FOREIGN KEY (quest_id) REFERENCES quests(id) ON DELETE CASCADE,
                               CONSTRAINT uq_quest_reviews_user_quest UNIQUE (quest_id, user_id)
);

CREATE INDEX idx_quest_review_quest_id    ON quest_reviews(quest_id);
CREATE INDEX idx_quest_review_user_id     ON quest_reviews(user_id);
CREATE INDEX idx_quest_review_visible     ON quest_reviews(is_visible);
CREATE INDEX idx_quest_review_created_at  ON quest_reviews(created_at);
CREATE INDEX idx_quest_review_rating      ON quest_reviews(rating);

COMMENT ON TABLE  quest_reviews            IS 'Таблица отзывов на квесты';
COMMENT ON COLUMN quest_reviews.rating     IS 'Рейтинг от 1 до 5';
COMMENT ON COLUMN quest_reviews.is_visible IS 'Видимость отзыва (для модерации)';