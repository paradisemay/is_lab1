package ru.ifmo.se.is_lab1.service.event;

import ru.ifmo.se.is_lab1.dto.MusicBandDto;

public class MusicBandEvent {
    private final MusicBandEventType type;
    private final MusicBandDto band;

    public MusicBandEvent(MusicBandEventType type, MusicBandDto band) {
        this.type = type;
        this.band = band;
    }

    public MusicBandEventType getType() {
        return type;
    }

    public MusicBandDto getBand() {
        return band;
    }
}
