package ru.ifmo.se.is_lab1.service.exception;

/**
 * Исключение, сигнализирующее о нарушении бизнес-ограничений уникальности
 * при создании или обновлении сущности {@code HumanBeing}.
 */
public class HumanBeingUniquenessException extends RuntimeException {

    public HumanBeingUniquenessException(String message) {
        super(message);
    }
}
