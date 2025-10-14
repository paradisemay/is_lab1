package ru.ifmo.se.is_lab1.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class ImpactSpeedCountRequest {

    @NotNull(message = "Порог обязателен")
    @DecimalMin(value = "0.0", inclusive = false, message = "Порог должен быть положительным")
    private BigDecimal threshold;

    public ImpactSpeedCountRequest() {
    }

    public ImpactSpeedCountRequest(BigDecimal threshold) {
        this.threshold = threshold;
    }

    public BigDecimal getThreshold() {
        return threshold;
    }

    public void setThreshold(BigDecimal threshold) {
        this.threshold = threshold;
    }
}
