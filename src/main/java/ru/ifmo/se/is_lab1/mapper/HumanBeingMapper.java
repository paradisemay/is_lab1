package ru.ifmo.se.is_lab1.mapper;

import org.springframework.stereotype.Component;

import ru.ifmo.se.is_lab1.dto.HumanBeingDto;
import ru.ifmo.se.is_lab1.model.Car;
import ru.ifmo.se.is_lab1.model.Coordinates;
import ru.ifmo.se.is_lab1.model.HumanBeing;
import ru.ifmo.se.is_lab1.dto.HumanBeingFormDto;

@Component
public class HumanBeingMapper {

    private final CarMapper carMapper;
    private final CoordinatesMapper coordinatesMapper;

    public HumanBeingMapper(CarMapper carMapper, CoordinatesMapper coordinatesMapper) {
        this.carMapper = carMapper;
        this.coordinatesMapper = coordinatesMapper;
    }

    public HumanBeingDto toDto(HumanBeing humanBeing) {
        if (humanBeing == null) {
            return null;
        }
        return new HumanBeingDto(
                humanBeing.getId(),
                humanBeing.getName(),
                coordinatesMapper.toDto(humanBeing.getCoordinates()),
                humanBeing.getCreationDate(),
                humanBeing.getRealHero(),
                humanBeing.getHasToothpick(),
                humanBeing.getImpactSpeed(),
                humanBeing.getSoundtrackName(),
                humanBeing.getWeaponType(),
                humanBeing.getMood(),
                carMapper.toDto(humanBeing.getCar())
        );
    }

    public void updateEntity(HumanBeing humanBeing,
                             HumanBeingFormDto form,
                             Coordinates coordinates,
                             Car car) {
        humanBeing.setName(form.getName());
        humanBeing.setCoordinates(coordinates);
        humanBeing.setRealHero(form.getRealHero());
        humanBeing.setHasToothpick(Boolean.TRUE.equals(form.getHasToothpick()));
        humanBeing.setImpactSpeed(form.getImpactSpeed());
        humanBeing.setSoundtrackName(form.getSoundtrackName());
        humanBeing.setWeaponType(form.getWeaponType());
        humanBeing.setMood(form.getMood());
        humanBeing.setCar(car);
    }
}
