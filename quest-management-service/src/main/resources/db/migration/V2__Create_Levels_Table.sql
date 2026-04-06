-- Создание таблицы levels
CREATE TABLE levels (
    id UUID PRIMARY KEY,
    quest_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    "order" INTEGER NOT NULL, -- порядковый номер уровня в квесте
    location VARCHAR(255),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    radius DECIMAL(8, 2), -- радиус в метрах
    media_ids UUID[], -- массив ID медиа файлов
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_levels_quest_id FOREIGN KEY (quest_id) REFERENCES quests(id) ON DELETE CASCADE,
    CONSTRAINT uq_levels_quest_order UNIQUE (quest_id, "order")
);

-- Создание индексов для таблицы levels
CREATE INDEX idx_levels_quest_id ON levels(quest_id);
CREATE INDEX idx_levels_order ON levels("order");
CREATE INDEX idx_levels_location ON levels(latitude, longitude);
CREATE INDEX idx_levels_created_at ON levels(created_at);
CREATE INDEX idx_levels_media_ids ON levels USING GIN(media_ids);

-- Создание триггера для обновления поля updated_at
CREATE TRIGGER update_levels_updated_at 
    BEFORE UPDATE ON levels 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Добавление комментариев к таблице и полям
COMMENT ON TABLE levels IS 'Таблица уровней квестов';
COMMENT ON COLUMN levels.id IS 'Уникальный идентификатор уровня';
COMMENT ON COLUMN levels.quest_id IS 'ID квеста';
COMMENT ON COLUMN levels.title IS 'Название уровня';
COMMENT ON COLUMN levels.description IS 'Описание уровня';
COMMENT ON COLUMN levels."order" IS 'Порядковый номер уровня в квесте';
COMMENT ON COLUMN levels.location IS 'Местоположение уровня';
COMMENT ON COLUMN levels.latitude IS 'Широта';
COMMENT ON COLUMN levels.longitude IS 'Долгота';
COMMENT ON COLUMN levels.radius IS 'Радиус в метрах';
COMMENT ON COLUMN levels.media_ids IS 'Массив ID медиа файлов';
COMMENT ON COLUMN levels.created_at IS 'Время создания';
COMMENT ON COLUMN levels.updated_at IS 'Время последнего обновления';