package ru.ifmo.se.is_lab1.service;

import org.springframework.dao.DataIntegrityViolationException;

import ru.ifmo.se.is_lab1.service.exception.HumanBeingUniquenessException;

public final class HumanBeingUniquenessResolver {

    private static final String NAME_SOUNDTRACK_CONSTRAINT = "human_being_name_soundtrack_uidx";
    private static final String REAL_HERO_IMPACT_CONSTRAINT = "human_being_real_hero_impact_speed_uidx";

    private HumanBeingUniquenessResolver() {
    }

    public static HumanBeingUniquenessException translate(DataIntegrityViolationException exception) {
        String message = exception.getMostSpecificCause() != null
                ? exception.getMostSpecificCause().getMessage()
                : exception.getMessage();
        String normalizedMessage = message != null ? message.toLowerCase() : "";

        if (normalizedMessage.contains(NAME_SOUNDTRACK_CONSTRAINT)) {
            return new HumanBeingUniquenessException(
                    "Комбинация имени и саундтрека должна быть уникальной (без учёта регистра)");
        }
        if (normalizedMessage.contains(REAL_HERO_IMPACT_CONSTRAINT)) {
            return new HumanBeingUniquenessException(
                    "Скорость удара настоящего героя должна быть уникальной");
        }
        return new HumanBeingUniquenessException("Нарушено ограничение уникальности человека");
    }
}
