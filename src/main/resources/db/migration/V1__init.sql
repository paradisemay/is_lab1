CREATE TABLE cars (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    model VARCHAR(255),
    color VARCHAR(64)
);

CREATE TABLE music_bands (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    impact_speed NUMERIC(10,2) NOT NULL,
    soundtrack_name VARCHAR(255) NOT NULL,
    mood VARCHAR(32) NOT NULL,
    creation_date TIMESTAMP WITH TIME ZONE NOT NULL,
    car_id BIGINT REFERENCES cars(id)
);

CREATE INDEX idx_music_bands_mood ON music_bands (mood);
CREATE INDEX idx_music_bands_soundtrack ON music_bands (soundtrack_name);
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
    creation_date      TIMESTAMPTZ      NOT NULL DEFAULT CURRENT_TIMESTAMP,
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
