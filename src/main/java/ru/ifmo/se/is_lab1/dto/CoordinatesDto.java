package ru.ifmo.se.is_lab1.dto;

public class CoordinatesDto {
    private Long id;
    private Integer x;
    private float y;

    public CoordinatesDto() {
    }

    public CoordinatesDto(Long id, Integer x, float y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
