-- Создание таблицы quests
CREATE TABLE quests (
    id BIGSERIAL PRIMARY KEY,
    number BIGINT UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    difficulty VARCHAR(50),
    quest_type VARCHAR(50),
    category VARCHAR(100),
    estimated_duration INTEGER, -- в минутах
    max_participants INTEGER,
    min_participants INTEGER,
    start_location VARCHAR(255),
    end_location VARCHAR(255),
    rules TEXT,
    prizes TEXT,
    requirements TEXT,
    tags TEXT[], -- массив тегов
    is_public BOOLEAN DEFAULT false,
    is_template BOOLEAN DEFAULT false,
    author_ids BIGINT[], -- массив ID авторов
    status VARCHAR(50) DEFAULT 'DRAFT',
    version INTEGER DEFAULT 1,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP,
    archived_at TIMESTAMP,
    archive_reason TEXT
);

-- Создание индексов для таблицы quests
CREATE INDEX idx_quests_number ON quests(number);
CREATE INDEX idx_quests_status ON quests(status);
CREATE INDEX idx_quests_difficulty ON quests(difficulty);
CREATE INDEX idx_quests_quest_type ON quests(quest_type);
CREATE INDEX idx_quests_category ON quests(category);
CREATE INDEX idx_quests_is_public ON quests(is_public);
CREATE INDEX idx_quests_is_template ON quests(is_template);
CREATE INDEX idx_quests_created_at ON quests(created_at);
CREATE INDEX idx_quests_published_at ON quests(published_at);
CREATE INDEX idx_quests_author_ids ON quests USING GIN(author_ids);
CREATE INDEX idx_quests_tags ON quests USING GIN(tags);

-- Создание триггера для обновления поля updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_quests_updated_at 
    BEFORE UPDATE ON quests 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Добавление комментариев к таблице и полям
COMMENT ON TABLE quests IS 'Таблица квестов';
COMMENT ON COLUMN quests.id IS 'Уникальный идентификатор квеста';
COMMENT ON COLUMN quests.number IS 'Уникальный номер квеста';
COMMENT ON COLUMN quests.title IS 'Название квеста';
COMMENT ON COLUMN quests.description IS 'Описание квеста';
COMMENT ON COLUMN quests.difficulty IS 'Сложность квеста';
COMMENT ON COLUMN quests.quest_type IS 'Тип квеста';
COMMENT ON COLUMN quests.category IS 'Категория квеста';
COMMENT ON COLUMN quests.estimated_duration IS 'Предполагаемая длительность в минутах';
COMMENT ON COLUMN quests.max_participants IS 'Максимальное количество участников';
COMMENT ON COLUMN quests.min_participants IS 'Минимальное количество участников';
COMMENT ON COLUMN quests.start_location IS 'Место начала квеста';
COMMENT ON COLUMN quests.end_location IS 'Место окончания квеста';
COMMENT ON COLUMN quests.rules IS 'Правила квеста';
COMMENT ON COLUMN quests.prizes IS 'Призы за прохождение';
COMMENT ON COLUMN quests.requirements IS 'Требования для участия';
COMMENT ON COLUMN quests.tags IS 'Теги квеста';
COMMENT ON COLUMN quests.is_public IS 'Флаг публичности квеста';
COMMENT ON COLUMN quests.is_template IS 'Флаг шаблона квеста';
COMMENT ON COLUMN quests.author_ids IS 'Массив ID авторов квеста';
COMMENT ON COLUMN quests.status IS 'Статус квеста (DRAFT, PUBLISHED, ACTIVE, ARCHIVED)';
COMMENT ON COLUMN quests.version IS 'Версия квеста';
COMMENT ON COLUMN quests.start_time IS 'Время начала квеста';
COMMENT ON COLUMN quests.end_time IS 'Время окончания квеста';
COMMENT ON COLUMN quests.created_at IS 'Время создания';
COMMENT ON COLUMN quests.updated_at IS 'Время последнего обновления';
COMMENT ON COLUMN quests.published_at IS 'Время публикации';
COMMENT ON COLUMN quests.archived_at IS 'Время архивации';
COMMENT ON COLUMN quests.archive_reason IS 'Причина архивации';