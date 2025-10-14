-- Align existing database with updated domain constraints

-- Coordinates adjustments
ALTER TABLE coordinates ADD COLUMN x_new INTEGER;
UPDATE coordinates SET x_new = CAST(ROUND(x) AS INTEGER);
ALTER TABLE coordinates ALTER COLUMN x_new SET NOT NULL;
ALTER TABLE coordinates ADD COLUMN y_new REAL;
UPDATE coordinates SET y_new = CAST(y AS REAL);
ALTER TABLE coordinates ALTER COLUMN y_new SET NOT NULL;
ALTER TABLE coordinates DROP COLUMN x;
ALTER TABLE coordinates DROP COLUMN y;
ALTER TABLE coordinates RENAME COLUMN x_new TO x;
ALTER TABLE coordinates RENAME COLUMN y_new TO y;
ALTER TABLE coordinates DROP CONSTRAINT IF EXISTS coordinates_y_non_negative;
ALTER TABLE coordinates ADD CONSTRAINT coordinates_y_non_negative CHECK (y >= 0);

-- Human being adjustments
ALTER TABLE human_being DROP CONSTRAINT IF EXISTS human_being_impact_speed_check;
ALTER TABLE human_being DROP CONSTRAINT IF EXISTS human_being_mood_check;
ALTER TABLE human_being DROP CONSTRAINT IF EXISTS human_being_weapon_type_check;

ALTER TABLE human_being ALTER COLUMN real_hero DROP NOT NULL;

ALTER TABLE human_being ADD COLUMN impact_speed_new INTEGER;
UPDATE human_being SET impact_speed_new = CAST(ROUND(impact_speed) AS INTEGER);
ALTER TABLE human_being ALTER COLUMN impact_speed_new SET NOT NULL;

ALTER TABLE human_being ADD COLUMN soundtrack_name_new VARCHAR(255);
UPDATE human_being SET soundtrack_name_new = LEFT(soundtrack_name, 255);
ALTER TABLE human_being ALTER COLUMN soundtrack_name_new SET NOT NULL;

ALTER TABLE human_being DROP COLUMN impact_speed;
ALTER TABLE human_being DROP COLUMN soundtrack_name;
ALTER TABLE human_being DROP COLUMN IF EXISTS minutes_of_waiting;
ALTER TABLE human_being RENAME COLUMN impact_speed_new TO impact_speed;
ALTER TABLE human_being RENAME COLUMN soundtrack_name_new TO soundtrack_name;

ALTER TABLE human_being ADD CONSTRAINT human_being_impact_speed_check CHECK (impact_speed > 0 AND impact_speed <= 907);
ALTER TABLE human_being ADD CONSTRAINT human_being_mood_check CHECK (mood IS NULL OR mood IN ('SADNESS', 'LONGING', 'GLOOM'));
ALTER TABLE human_being ADD CONSTRAINT human_being_weapon_type_check CHECK (
    weapon_type IS NULL OR weapon_type IN (
                                           'SHOTGUN',
                                           'KNIFE',
                                           'MACHINE_GUN',
                                           'BAT'
        )
    );