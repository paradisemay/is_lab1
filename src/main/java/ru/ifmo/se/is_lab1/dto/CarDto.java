package ru.ifmo.se.is_lab1.dto;

public class CarDto {
    private Long id;
    private String name;
    private String model;
    private String color;

    public CarDto() {
    }

    public CarDto(Long id, String name, String model, String color) {
        this.id = id;
        this.name = name;
        this.model = model;
        this.color = color;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
