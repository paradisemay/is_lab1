package ru.ifmo.se.is_lab1.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Entity(name = "legacy_coordinates")
@Table(name = "coordinates")
public class Coordinates {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "x", nullable = false)
    private Integer x;

    @PositiveOrZero
    @Column(name = "y", nullable = false)
    private float y;

    protected Coordinates() {
    }

    public Coordinates(Integer x, float y) {
        this.x = x;
        this.y = y;
    }

    public Long getId() {
        return id;
    }

    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }
}
