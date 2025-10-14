package ru.ifmo.se.is_lab1.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import ru.ifmo.se.is_lab1.model.Mood;
import ru.ifmo.se.is_lab1.model.WeaponType;

public class HumanBeingFormDto {

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotNull
    private Integer coordinatesX;

    @PositiveOrZero
    private float coordinatesY;

    private Boolean realHero;

    @NotNull
    private Boolean hasToothpick;

    @NotNull
    @Min(1)
    @Max(907)
    private Integer impactSpeed;

    @NotBlank
    @Size(max = 255)
    private String soundtrackName;

    private WeaponType weaponType;

    private Mood mood;

    private Long carId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCoordinatesX() {
        return coordinatesX;
    }

    public void setCoordinatesX(Integer coordinatesX) {
        this.coordinatesX = coordinatesX;
    }

    public float getCoordinatesY() {
        return coordinatesY;
    }

    public void setCoordinatesY(float coordinatesY) {
        this.coordinatesY = coordinatesY;
    }

    public Boolean getRealHero() {
        return realHero;
    }

    public void setRealHero(Boolean realHero) {
        this.realHero = realHero;
    }

    public Boolean getHasToothpick() {
        return hasToothpick;
    }

    public void setHasToothpick(Boolean hasToothpick) {
        this.hasToothpick = hasToothpick;
    }

    public Integer getImpactSpeed() {
        return impactSpeed;
    }

    public void setImpactSpeed(Integer impactSpeed) {
        this.impactSpeed = impactSpeed;
    }

    public String getSoundtrackName() {
        return soundtrackName;
    }

    public void setSoundtrackName(String soundtrackName) {
        this.soundtrackName = soundtrackName;
    }

    public WeaponType getWeaponType() {
        return weaponType;
    }

    public void setWeaponType(WeaponType weaponType) {
        this.weaponType = weaponType;
    }

    public Mood getMood() {
        return mood;
    }

    public void setMood(Mood mood) {
        this.mood = mood;
    }

    public Long getCarId() {
        return carId;
    }

    public void setCarId(Long carId) {
        this.carId = carId;
    }
}
