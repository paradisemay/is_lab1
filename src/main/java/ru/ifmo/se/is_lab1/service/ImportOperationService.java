package ru.ifmo.se.is_lab1.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ru.ifmo.se.is_lab1.domain.ImportOperation;
import ru.ifmo.se.is_lab1.dto.ImportOperationDto;
import ru.ifmo.se.is_lab1.repository.ImportOperationRepository;
import ru.ifmo.se.is_lab1.service.security.CurrentUserService;

@Service
public class ImportOperationService {

    private final ImportOperationRepository importOperationRepository;
    private final CurrentUserService currentUserService;

    public ImportOperationService(ImportOperationRepository importOperationRepository,
                                  CurrentUserService currentUserService) {
        this.importOperationRepository = importOperationRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ImportOperation startOperation() {
        String initiator = currentUserService.getCurrentUsername();
        ImportOperation operation = new ImportOperation(initiator, ImportOperation.Status.STARTED);
        return importOperationRepository.save(operation);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSuccess(Long operationId, int addedCount) {
        ImportOperation operation = importOperationRepository.findById(operationId)
                .orElseThrow(() -> new IllegalArgumentException("Import operation not found: " + operationId));
        operation.setStatus(ImportOperation.Status.SUCCESS);
        operation.setAddedCount(addedCount);
        operation.setErrorMessage(null);
        importOperationRepository.save(operation);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailure(Long operationId, String errorMessage) {
        ImportOperation operation = importOperationRepository.findById(operationId)
                .orElseThrow(() -> new IllegalArgumentException("Import operation not found: " + operationId));
        operation.setStatus(ImportOperation.Status.FAILED);
        operation.setAddedCount(null);
        operation.setErrorMessage(errorMessage);
        importOperationRepository.save(operation);
    }

    @Transactional(readOnly = true)
    public Page<ImportOperationDto> findHistory(Pageable pageable) {
        if (currentUserService.isAdmin()) {
            return importOperationRepository.findAllByOrderByCreatedAtDesc(pageable)
                    .map(ImportOperationDto::fromEntity);
        }
        String username = currentUserService.getCurrentUsername();
        Page<ImportOperation> page = importOperationRepository.findByInitiatorOrderByCreatedAtDesc(username, pageable);
        return page.map(ImportOperationDto::fromEntity);
    }
}
