package ru.ifmo.se.is_lab1.dto;

import ru.ifmo.se.is_lab1.domain.Mood;

import java.math.BigDecimal;
import java.util.Optional;

public class MusicBandFilter {
    private String name;
    private Mood mood;
    private BigDecimal minImpactSpeed;
    private BigDecimal maxImpactSpeed;
    private String soundtrackPrefix;
    private Long carId;

    public Optional<String> nameOptional() {
        return Optional.ofNullable(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Optional<Mood> moodOptional() {
        return Optional.ofNullable(mood);
    }

    public Mood getMood() {
        return mood;
    }

    public void setMood(Mood mood) {
        this.mood = mood;
    }

    public Optional<BigDecimal> minImpactSpeedOptional() {
        return Optional.ofNullable(minImpactSpeed);
    }

    public BigDecimal getMinImpactSpeed() {
        return minImpactSpeed;
    }

    public void setMinImpactSpeed(BigDecimal minImpactSpeed) {
        this.minImpactSpeed = minImpactSpeed;
    }

    public Optional<BigDecimal> maxImpactSpeedOptional() {
        return Optional.ofNullable(maxImpactSpeed);
    }

    public BigDecimal getMaxImpactSpeed() {
        return maxImpactSpeed;
    }

    public void setMaxImpactSpeed(BigDecimal maxImpactSpeed) {
        this.maxImpactSpeed = maxImpactSpeed;
    }

    public Optional<String> soundtrackPrefixOptional() {
        return Optional.ofNullable(soundtrackPrefix);
    }

    public String getSoundtrackPrefix() {
        return soundtrackPrefix;
    }

    public void setSoundtrackPrefix(String soundtrackPrefix) {
        this.soundtrackPrefix = soundtrackPrefix;
    }

    public Optional<Long> carIdOptional() {
        return Optional.ofNullable(carId);
    }

    public Long getCarId() {
        return carId;
    }

    public void setCarId(Long carId) {
        this.carId = carId;
    }
}
