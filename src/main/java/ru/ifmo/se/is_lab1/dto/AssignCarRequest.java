package ru.ifmo.se.is_lab1.dto;

import jakarta.validation.constraints.NotNull;

public class AssignCarRequest {

    @NotNull
    private Long carId;

    public Long getCarId() {
        return carId;
    }

    public void setCarId(Long carId) {
        this.carId = carId;
    }
}
