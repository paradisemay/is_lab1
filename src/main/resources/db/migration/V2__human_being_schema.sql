-- Replace legacy MusicBand tables with the HumanBeing domain model
DROP TABLE IF EXISTS music_bands;
DROP TABLE IF EXISTS cars;

CREATE TABLE coordinates (
    id           BIGSERIAL PRIMARY KEY,
    x            DOUBLE PRECISION NOT NULL,
    y            DOUBLE PRECISION NOT NULL
);

CREATE TABLE car (
    id   BIGSERIAL PRIMARY KEY,
    name TEXT    NOT NULL,
    cool BOOLEAN NOT NULL
);

CREATE TABLE human_being (
    id                 BIGSERIAL PRIMARY KEY,
    name               TEXT             NOT NULL,
    coordinates_id     BIGINT           NOT NULL REFERENCES coordinates (id) ON DELETE RESTRICT,
    creation_date      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    real_hero          BOOLEAN          NOT NULL,
    has_toothpick      BOOLEAN          NOT NULL,
    impact_speed       DOUBLE PRECISION NOT NULL,
    soundtrack_name    TEXT             NOT NULL,
    minutes_of_waiting BIGINT           NOT NULL,
    mood               VARCHAR(32),
    weapon_type        VARCHAR(32),
    car_id             BIGINT REFERENCES car (id) ON DELETE SET NULL,
    CHECK (impact_speed <= 907),
    CHECK (mood IS NULL OR mood IN ('SORROW', 'GLOOM', 'APATHY', 'CALM', 'RAGE')),
    CHECK (weapon_type IS NULL OR weapon_type IN ('HAMMER', 'AXE', 'RIFLE', 'SHOTGUN', 'BAT'))
);

CREATE INDEX human_being_coordinates_idx ON human_being (coordinates_id);
CREATE INDEX human_being_car_idx ON human_being (car_id);
CREATE INDEX human_being_mood_idx ON human_being (mood);
CREATE INDEX human_being_weapon_idx ON human_being (weapon_type);
CREATE INDEX human_being_soundtrack_idx ON human_being (soundtrack_name);
