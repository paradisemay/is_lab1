package ru.ifmo.se.is_lab1.mapper;

import org.springframework.stereotype.Component;
import ru.ifmo.se.is_lab1.domain.Car;
import ru.ifmo.se.is_lab1.dto.CarDto;

@Component
public class CarMapper {
    public CarDto toDto(Car car) {
        if (car == null) {
            return null;
        }
        return new CarDto(car.getId(), car.getName(), car.getModel(), car.getColor());
    }
}
