package ru.ifmo.se.is_lab1.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Embeddable
public class Coordinates {

    @NotNull
    @Column(name = "coordinate_x", nullable = false)
    private Double x;

    @NotNull
    @PositiveOrZero
    @Column(name = "coordinate_y", nullable = false)
    private Double y;

    protected Coordinates() {
    }

    public Coordinates(Double x, Double y) {
        this.x = x;
        this.y = y;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }
}
