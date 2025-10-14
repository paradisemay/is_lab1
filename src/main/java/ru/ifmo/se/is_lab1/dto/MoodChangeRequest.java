package ru.ifmo.se.is_lab1.dto;

import jakarta.validation.constraints.NotNull;
import ru.ifmo.se.is_lab1.domain.Mood;

public class MoodChangeRequest {

    @NotNull
    private Mood sourceMood;

    @NotNull
    private Mood targetMood;

    public Mood getSourceMood() {
        return sourceMood;
    }

    public void setSourceMood(Mood sourceMood) {
        this.sourceMood = sourceMood;
    }

    public Mood getTargetMood() {
        return targetMood;
    }

    public void setTargetMood(Mood targetMood) {
        this.targetMood = targetMood;
    }
}
