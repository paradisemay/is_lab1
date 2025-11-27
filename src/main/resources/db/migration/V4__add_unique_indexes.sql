ALTER TABLE human_being ADD COLUMN name_normalized VARCHAR(255);
ALTER TABLE human_being ADD COLUMN soundtrack_name_normalized VARCHAR(255);
ALTER TABLE human_being ADD COLUMN real_hero_impact_key INTEGER;

UPDATE human_being
SET name_normalized = lower(name),
    soundtrack_name_normalized = lower(soundtrack_name),
    real_hero_impact_key = CASE WHEN real_hero = TRUE THEN impact_speed END;

WITH duplicates AS (
    SELECT id,
           row_number() OVER (PARTITION BY lower(name), lower(soundtrack_name) ORDER BY id) AS rn
    FROM human_being
)
DELETE FROM human_being hb
USING duplicates d
WHERE hb.id = d.id
  AND d.rn > 1;

CREATE UNIQUE INDEX human_being_name_soundtrack_uidx
    ON human_being (name_normalized, soundtrack_name_normalized);

CREATE UNIQUE INDEX human_being_real_hero_impact_speed_uidx
    ON human_being (real_hero_impact_key);
