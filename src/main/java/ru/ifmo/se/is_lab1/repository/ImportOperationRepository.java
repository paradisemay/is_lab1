package ru.ifmo.se.is_lab1.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import ru.ifmo.se.is_lab1.domain.ImportOperation;

public interface ImportOperationRepository extends JpaRepository<ImportOperation, Long> {

    Page<ImportOperation> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<ImportOperation> findByInitiatorOrderByCreatedAtDesc(String initiator, Pageable pageable);
}
