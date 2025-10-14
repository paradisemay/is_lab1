package ru.ifmo.se.is_lab1.mapper;

import org.springframework.stereotype.Component;
import ru.ifmo.se.is_lab1.dto.CarOptionDto;
import ru.ifmo.se.is_lab1.dto.CoordinatesDto;
import ru.ifmo.se.is_lab1.dto.HumanBeingDto;
import ru.ifmo.se.is_lab1.model.Car;
import ru.ifmo.se.is_lab1.model.Coordinates;
import ru.ifmo.se.is_lab1.model.HumanBeing;

@Component
public class HumanBeingMapper {

    public HumanBeingDto toDto(HumanBeing human) {
        if (human == null) {
            return null;
        }
        return new HumanBeingDto(
                human.getId(),
                human.getName(),
                toDto(human.getCoordinates()),
                human.getCreationDate(),
                human.isRealHero(),
                human.getHasToothpick(),
                human.getImpactSpeed(),
                human.getSoundtrackName(),
                human.getMinutesOfWaiting(),
                human.getWeaponType(),
                human.getMood(),
                toDto(human.getCar())
        );
    }

    private CoordinatesDto toDto(Coordinates coordinates) {
        if (coordinates == null) {
            return null;
        }
        return new CoordinatesDto(coordinates.getId(), coordinates.getX(), coordinates.getY());
    }

    private CarOptionDto toDto(Car car) {
        if (car == null) {
            return null;
        }
        return new CarOptionDto(car.getId(), car.getName(), car.getCool());
    }
}
