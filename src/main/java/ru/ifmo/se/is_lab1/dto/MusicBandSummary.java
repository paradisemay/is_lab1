package ru.ifmo.se.is_lab1.dto;

import java.math.BigDecimal;

public class MusicBandSummary {
    private final long totalCount;
    private final BigDecimal totalImpactSpeed;

    public MusicBandSummary(long totalCount, BigDecimal totalImpactSpeed) {
        this.totalCount = totalCount;
        this.totalImpactSpeed = totalImpactSpeed;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public BigDecimal getTotalImpactSpeed() {
        return totalImpactSpeed;
    }
}
