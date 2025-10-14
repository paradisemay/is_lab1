package ru.ifmo.se.is_lab1.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ImpactSpeedCountRequest {

    @NotNull(message = "Порог обязателен")
    @Min(value = 1, message = "Порог должен быть положительным")
    @Max(value = 907, message = "Максимальное значение скорости удара — 907")
    private Integer threshold;

    public ImpactSpeedCountRequest() {
    }

    public ImpactSpeedCountRequest(Integer threshold) {
        this.threshold = threshold;
    }

    public Integer getThreshold() {
        return threshold;
    }

    public void setThreshold(Integer threshold) {
        this.threshold = threshold;
    }
}
