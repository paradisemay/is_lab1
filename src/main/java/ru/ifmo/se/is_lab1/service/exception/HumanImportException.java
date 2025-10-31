package ru.ifmo.se.is_lab1.service.exception;

public class HumanImportException extends RuntimeException {

    public HumanImportException(String message) {
        super(message);
    }

    public HumanImportException(String message, Throwable cause) {
        super(message, cause);
    }
}
