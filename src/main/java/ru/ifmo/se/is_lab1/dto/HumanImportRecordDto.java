package ru.ifmo.se.is_lab1.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import ru.ifmo.se.is_lab1.model.Mood;
import ru.ifmo.se.is_lab1.model.WeaponType;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HumanImportRecordDto {

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotNull
    @Valid
    private Coordinates coordinates;

    private Boolean realHero;

    @NotNull
    private Boolean hasToothpick;

    @NotNull
    @Positive
    @Max(907)
    private Integer impactSpeed;

    @NotBlank
    @Size(max = 255)
    private String soundtrackName;

    private WeaponType weaponType;

    private Mood mood;

    @Valid
    private Car car;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
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

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Coordinates {

        @NotNull
        private Integer x;

        @NotNull
        @PositiveOrZero
        private Float y;

        public Integer getX() {
            return x;
        }

        public void setX(Integer x) {
            this.x = x;
        }

        public Float getY() {
            return y;
        }

        public void setY(Float y) {
            this.y = y;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Car {

        @NotBlank
        @Size(max = 120)
        private String name;

        @NotNull
        private Boolean cool;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Boolean getCool() {
            return cool;
        }

        public void setCool(Boolean cool) {
            this.cool = cool;
        }
    }
}
