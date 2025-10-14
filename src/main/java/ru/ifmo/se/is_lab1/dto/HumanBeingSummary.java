package ru.ifmo.se.is_lab1.dto;

public class HumanBeingSummary {
    private final long totalCount;
    private final long totalImpactSpeed;

    public HumanBeingSummary(long totalCount, long totalImpactSpeed) {
        this.totalCount = totalCount;
        this.totalImpactSpeed = totalImpactSpeed;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public long getTotalImpactSpeed() {
        return totalImpactSpeed;
    }
}
