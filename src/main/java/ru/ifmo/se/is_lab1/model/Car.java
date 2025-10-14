package ru.ifmo.se.is_lab1.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity(name = "legacy_car")
@Table(name = "car")
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 120)
    @Column(name = "name", nullable = false, unique = true, length = 120)
    private String name;

    @NotNull
    @Column(name = "cool", nullable = false)
    private Boolean cool;

    protected Car() {
    }

    public Car(String name, Boolean cool) {
        this.name = name;
        this.cool = cool;
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

    public Boolean getCool() {
        return cool;
    }

    public void setCool(Boolean cool) {
        this.cool = cool;
    }
}
