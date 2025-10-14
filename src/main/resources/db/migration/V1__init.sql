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
