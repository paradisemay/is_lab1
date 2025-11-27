package ru.ifmo.se.is_lab1.domain;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import ru.ifmo.se.is_lab1.model.Mood;
import ru.ifmo.se.is_lab1.model.WeaponType;

@Entity
@Table(name = "human_being")
public class HumanBeing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Valid
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coordinates_id", nullable = false)
    private Coordinates coordinates;

    @CreationTimestamp
    @Column(name = "creation_date", nullable = false, updatable = false)
    private Instant creationDate;

    @Column(name = "real_hero")
    private Boolean realHero;

    @Column(name = "has_toothpick", nullable = false)
    private boolean hasToothpick;

    @Positive
    @Max(907)
    @Column(name = "impact_speed", nullable = false)
    private int impactSpeed;

    @NotBlank
    @Size(max = 255)
    @Column(name = "soundtrack_name", nullable = false, length = 255)
    private String soundtrackName;

    @Enumerated(EnumType.STRING)
    @Column(name = "weapon_type", length = 32)
    private WeaponType weaponType;

    @Enumerated(EnumType.STRING)
    @Column(name = "mood", length = 32)
    private Mood mood;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id")
    private Car car;

    @Column(name = "name_normalized", length = 255)
    private String nameNormalized;

    @Column(name = "soundtrack_name_normalized", length = 255)
    private String soundtrackNameNormalized;

    @Column(name = "real_hero_impact_key")
    private Integer realHeroImpactKey;

    protected HumanBeing() {
    }

    public HumanBeing(String name,
                      Coordinates coordinates,
                      Boolean realHero,
                      boolean hasToothpick,
                      int impactSpeed,
                      String soundtrackName,
                      WeaponType weaponType,
                      Mood mood,
                      Car car) {
        this.name = name;
        this.coordinates = coordinates;
        this.realHero = realHero;
        this.hasToothpick = hasToothpick;
        this.impactSpeed = impactSpeed;
        this.soundtrackName = soundtrackName;
        this.weaponType = weaponType;
        this.mood = mood;
        this.car = car;
        updateDerivedFields();
    }

    public Long getId() {
        return id;
    }

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

    public Instant getCreationDate() {
        return creationDate;
    }

    public Boolean getRealHero() {
        return realHero;
    }

    public void setRealHero(Boolean realHero) {
        this.realHero = realHero;
    }

    public boolean getHasToothpick() {
        return hasToothpick;
    }

    public void setHasToothpick(boolean hasToothpick) {
        this.hasToothpick = hasToothpick;
    }

    public int getImpactSpeed() {
        return impactSpeed;
    }

    public void setImpactSpeed(int impactSpeed) {
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

    @PrePersist
    @PreUpdate
    private void updateDerivedFields() {
        this.nameNormalized = name != null ? name.trim().toLowerCase() : null;
        this.soundtrackNameNormalized = soundtrackName != null ? soundtrackName.trim().toLowerCase() : null;
        this.realHeroImpactKey = Boolean.TRUE.equals(realHero) ? impactSpeed : null;
    }
}
