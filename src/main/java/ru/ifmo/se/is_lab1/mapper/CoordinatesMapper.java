package ru.ifmo.se.is_lab1.mapper;

import org.springframework.stereotype.Component;
import ru.ifmo.se.is_lab1.dto.CoordinatesDto;
import ru.ifmo.se.is_lab1.domain.Coordinates;

@Component
public class CoordinatesMapper {

    public CoordinatesDto toDto(Coordinates coordinates) {
        if (coordinates == null) {
            return null;
        }
        return new CoordinatesDto(coordinates.getId(), coordinates.getX(), coordinates.getY());
    }
}
