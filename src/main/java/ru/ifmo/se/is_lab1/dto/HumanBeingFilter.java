package ru.ifmo.se.is_lab1.dto;

import java.util.Optional;

import ru.ifmo.se.is_lab1.model.Mood;
import ru.ifmo.se.is_lab1.model.WeaponType;

public class HumanBeingFilter {
    private String name;
    private Mood mood;
    private Integer minImpactSpeed;
    private Integer maxImpactSpeed;
    private String soundtrackPrefix;
    private Long carId;
    private Boolean realHero;
    private Boolean hasToothpick;
    private WeaponType weaponType;

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

    public Optional<Integer> minImpactSpeedOptional() {
        return Optional.ofNullable(minImpactSpeed);
    }

    public Integer getMinImpactSpeed() {
        return minImpactSpeed;
    }

    public void setMinImpactSpeed(Integer minImpactSpeed) {
        this.minImpactSpeed = minImpactSpeed;
    }

    public Optional<Integer> maxImpactSpeedOptional() {
        return Optional.ofNullable(maxImpactSpeed);
    }

    public Integer getMaxImpactSpeed() {
        return maxImpactSpeed;
    }

    public void setMaxImpactSpeed(Integer maxImpactSpeed) {
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

    public Optional<WeaponType> weaponTypeOptional() {
        return Optional.ofNullable(weaponType);
    }

    public WeaponType getWeaponType() {
        return weaponType;
    }

    public void setWeaponType(WeaponType weaponType) {
        this.weaponType = weaponType;
    }
}
