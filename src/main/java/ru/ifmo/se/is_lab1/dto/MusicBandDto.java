package ru.ifmo.se.is_lab1.dto;

import ru.ifmo.se.is_lab1.domain.Mood;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class MusicBandDto {
    private Long id;
    private String name;
    private BigDecimal impactSpeed;
    private String soundtrackName;
    private Mood mood;
    private OffsetDateTime creationDate;
    private CarDto car;

    public MusicBandDto() {
    }

    public MusicBandDto(Long id, String name, BigDecimal impactSpeed, String soundtrackName, Mood mood,
                        OffsetDateTime creationDate, CarDto car) {
        this.id = id;
        this.name = name;
        this.impactSpeed = impactSpeed;
        this.soundtrackName = soundtrackName;
        this.mood = mood;
        this.creationDate = creationDate;
        this.car = car;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getImpactSpeed() {
        return impactSpeed;
    }

    public void setImpactSpeed(BigDecimal impactSpeed) {
        this.impactSpeed = impactSpeed;
    }

    public String getSoundtrackName() {
        return soundtrackName;
    }

    public void setSoundtrackName(String soundtrackName) {
        this.soundtrackName = soundtrackName;
    }

    public Mood getMood() {
        return mood;
    }

    public void setMood(Mood mood) {
        this.mood = mood;
    }

    public OffsetDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(OffsetDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public CarDto getCar() {
        return car;
    }

    public void setCar(CarDto car) {
        this.car = car;
    }
}
