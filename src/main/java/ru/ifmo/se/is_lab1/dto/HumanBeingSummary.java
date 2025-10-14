package ru.ifmo.se.is_lab1.dto;

public class HumanBeingSummary {
    private final long totalCount;
    private final double totalImpactSpeed;

    public HumanBeingSummary(long totalCount, double totalImpactSpeed) {
        this.totalCount = totalCount;
        this.totalImpactSpeed = totalImpactSpeed;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public double getTotalImpactSpeed() {
        return totalImpactSpeed;
    }
}
