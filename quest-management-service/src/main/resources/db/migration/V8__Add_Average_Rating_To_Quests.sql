-- Добавление поля среднего рейтинга в таблицу quests
ALTER TABLE quests ADD COLUMN average_rating DECIMAL(3,2) DEFAULT 0.0;

-- Создание индекса для среднего рейтинга
CREATE INDEX idx_quests_average_rating ON quests(average_rating);

-- Добавление комментария
COMMENT ON COLUMN quests.average_rating IS 'Средний рейтинг квеста (от 0.00 до 5.00)';

-- Создание функции для обновления среднего рейтинга
CREATE OR REPLACE FUNCTION update_quest_average_rating(quest_id_param BIGINT)
RETURNS VOID AS $$
DECLARE
    avg_rating DECIMAL(3,2);
BEGIN
    SELECT COALESCE(AVG(rating), 0.0)::DECIMAL(3,2) 
    INTO avg_rating 
    FROM quest_ratings 
    WHERE quest_id = quest_id_param;
    
    UPDATE quests 
    SET average_rating = avg_rating 
    WHERE id = quest_id_param;
END;
$$ LANGUAGE plpgsql;

-- Создание триггера для автоматического обновления среднего рейтинга
CREATE OR REPLACE FUNCTION quest_rating_trigger()
RETURNS TRIGGER AS $$
BEGIN
    -- После вставки или обновления рейтинга
    IF TG_OP = 'INSERT' OR TG_OP = 'UPDATE' THEN
        PERFORM update_quest_average_rating(NEW.quest_id);
        RETURN NEW;
    -- После удаления рейтинга
    ELSIF TG_OP = 'DELETE' THEN
        PERFORM update_quest_average_rating(OLD.quest_id);
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Создание триггеров
CREATE TRIGGER quest_rating_insert_trigger
    AFTER INSERT ON quest_ratings
    FOR EACH ROW
    EXECUTE FUNCTION quest_rating_trigger();

CREATE TRIGGER quest_rating_update_trigger
    AFTER UPDATE ON quest_ratings
    FOR EACH ROW
    EXECUTE FUNCTION quest_rating_trigger();

CREATE TRIGGER quest_rating_delete_trigger
    AFTER DELETE ON quest_ratings
    FOR EACH ROW
    EXECUTE FUNCTION quest_rating_trigger();

-- Обновление среднего рейтинга для существующих квестов
UPDATE quests 
SET average_rating = COALESCE(
    (SELECT AVG(rating)::DECIMAL(3,2) 
     FROM quest_ratings 
     WHERE quest_ratings.quest_id = quests.id), 
    0.0
);