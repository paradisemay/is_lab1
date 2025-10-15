package ru.ifmo.se.is_lab1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SoundtrackSearchRequest {

    @NotBlank(message = "Префикс обязателен")
    @Size(max = 255, message = "Максимальная длина префикса — 255 символов")
    private String prefix;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
