package ru.ifmo.se.is_lab1.dto;

import java.time.OffsetDateTime;

import ru.ifmo.se.is_lab1.domain.ImportOperation;

public class ImportOperationDto {

    private Long id;
    private String initiator;
    private ImportOperation.Status status;
    private Integer addedCount;
    private String errorMessage;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public ImportOperationDto() {
    }

    public ImportOperationDto(Long id,
                              String initiator,
                              ImportOperation.Status status,
                              Integer addedCount,
                              String errorMessage,
                              OffsetDateTime createdAt,
                              OffsetDateTime updatedAt) {
        this.id = id;
        this.initiator = initiator;
        this.status = status;
        this.addedCount = addedCount;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ImportOperationDto fromEntity(ImportOperation entity) {
        return new ImportOperationDto(
                entity.getId(),
                entity.getInitiator(),
                entity.getStatus(),
                entity.getAddedCount(),
                entity.getErrorMessage(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInitiator() {
        return initiator;
    }

    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }

    public ImportOperation.Status getStatus() {
        return status;
    }

    public void setStatus(ImportOperation.Status status) {
        this.status = status;
    }

    public Integer getAddedCount() {
        return addedCount;
    }

    public void setAddedCount(Integer addedCount) {
        this.addedCount = addedCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
