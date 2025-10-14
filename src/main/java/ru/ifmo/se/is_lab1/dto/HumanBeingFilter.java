package ru.ifmo.se.is_lab1.dto;

import java.util.Optional;

import ru.ifmo.se.is_lab1.model.Mood;
import ru.ifmo.se.is_lab1.model.WeaponType;

public class HumanBeingFilter {
    private String name;
    private Mood mood;
    private WeaponType weaponType;
    private Boolean realHero;
    private Boolean hasToothpick;
    private Double minImpactSpeed;
    private Double maxImpactSpeed;
    private Long minMinutesOfWaiting;
    private Long maxMinutesOfWaiting;
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

    public Optional<WeaponType> weaponTypeOptional() {
        return Optional.ofNullable(weaponType);
    }

    public WeaponType getWeaponType() {
        return weaponType;
    }

    public void setWeaponType(WeaponType weaponType) {
        this.weaponType = weaponType;
    }

    public Optional<Boolean> realHeroOptional() {
        return Optional.ofNullable(realHero);
    }

    public Boolean getRealHero() {
        return realHero;
    }

    public void setRealHero(Boolean realHero) {
        this.realHero = realHero;
    }

    public Optional<Boolean> hasToothpickOptional() {
        return Optional.ofNullable(hasToothpick);
    }

    public Boolean getHasToothpick() {
        return hasToothpick;
    }

    public void setHasToothpick(Boolean hasToothpick) {
        this.hasToothpick = hasToothpick;
    }

    public Optional<Double> minImpactSpeedOptional() {
        return Optional.ofNullable(minImpactSpeed);
    }

    public Double getMinImpactSpeed() {
        return minImpactSpeed;
    }

    public void setMinImpactSpeed(Double minImpactSpeed) {
        this.minImpactSpeed = minImpactSpeed;
    }

    public Optional<Double> maxImpactSpeedOptional() {
        return Optional.ofNullable(maxImpactSpeed);
    }

    public Double getMaxImpactSpeed() {
        return maxImpactSpeed;
    }

    public void setMaxImpactSpeed(Double maxImpactSpeed) {
        this.maxImpactSpeed = maxImpactSpeed;
    }

    public Optional<Long> minMinutesOfWaitingOptional() {
        return Optional.ofNullable(minMinutesOfWaiting);
    }

    public Long getMinMinutesOfWaiting() {
        return minMinutesOfWaiting;
    }

    public void setMinMinutesOfWaiting(Long minMinutesOfWaiting) {
        this.minMinutesOfWaiting = minMinutesOfWaiting;
    }

    public Optional<Long> maxMinutesOfWaitingOptional() {
        return Optional.ofNullable(maxMinutesOfWaiting);
    }

    public Long getMaxMinutesOfWaiting() {
        return maxMinutesOfWaiting;
    }

    public void setMaxMinutesOfWaiting(Long maxMinutesOfWaiting) {
        this.maxMinutesOfWaiting = maxMinutesOfWaiting;
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
