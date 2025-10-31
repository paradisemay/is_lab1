package ru.ifmo.se.is_lab1.controller;

import java.util.Map;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import ru.ifmo.se.is_lab1.service.exception.HumanImportException;

@ControllerAdvice(assignableTypes = {HumanBeingRestController.class})
public class RestExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a));
        return ResponseEntity.badRequest().body(Map.of(
                "message", "Ошибка валидации",
                "errors", errors
        ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(violation -> violation.getPropertyPath().toString(), ConstraintViolation::getMessage,
                        (a, b) -> a));
        return ResponseEntity.badRequest().body(Map.of(
                "message", "Ошибка валидации",
                "errors", errors
        ));
    }

    @ExceptionHandler(HumanImportException.class)
    public ResponseEntity<Map<String, Object>> handleImportException(HumanImportException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "message", "Ошибка импорта",
                "error", ex.getMessage()
        ));
    }
}
