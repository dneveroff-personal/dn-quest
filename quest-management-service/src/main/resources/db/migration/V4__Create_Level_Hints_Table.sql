-- Создание таблицы level_hints
CREATE TABLE level_hints (
    id BIGSERIAL PRIMARY KEY,
    level_id BIGINT NOT NULL,
    text TEXT NOT NULL,
    cost INTEGER DEFAULT 0, -- стоимость в очках
    available_after TIMESTAMP, -- время, после которого подсказка доступна
    active BOOLEAN DEFAULT true,
    used_by BIGINT[], -- массив ID пользователей и времени использования (JSON)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_level_hints_level_id FOREIGN KEY (level_id) REFERENCES levels(id) ON DELETE CASCADE
);

-- Создание индексов для таблицы level_hints
CREATE INDEX idx_level_hints_level_id ON level_hints(level_id);
CREATE INDEX idx_level_hints_cost ON level_hints(cost);
CREATE INDEX idx_level_hints_available_after ON level_hints(available_after);
CREATE INDEX idx_level_hints_active ON level_hints(active);
CREATE INDEX idx_level_hints_created_at ON level_hints(created_at);

-- Создание триггера для обновления поля updated_at
CREATE TRIGGER update_level_hints_updated_at 
    BEFORE UPDATE ON level_hints 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Добавление комментариев к таблице и полям
COMMENT ON TABLE level_hints IS 'Таблица подсказок уровней';
COMMENT ON COLUMN level_hints.id IS 'Уникальный идентификатор подсказки';
COMMENT ON COLUMN level_hints.level_id IS 'ID уровня';
COMMENT ON COLUMN level_hints.text IS 'Текст подсказки';
COMMENT ON COLUMN level_hints.cost IS 'Стоимость подсказки в очках';
COMMENT ON COLUMN level_hints.available_after IS 'Время, после которого подсказка доступна';
COMMENT ON COLUMN level_hints.active IS 'Флаг активности подсказки';
COMMENT ON COLUMN level_hints.used_by IS 'Массив ID пользователей и времени использования';
COMMENT ON COLUMN level_hints.created_at IS 'Время создания';
COMMENT ON COLUMN level_hints.updated_at IS 'Время последнего обновления';