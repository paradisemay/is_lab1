package ru.ifmo.se.is_lab1.mapper;

import org.springframework.stereotype.Component;
import ru.ifmo.se.is_lab1.domain.Car;
import ru.ifmo.se.is_lab1.domain.MusicBand;
import ru.ifmo.se.is_lab1.dto.MusicBandDto;
import ru.ifmo.se.is_lab1.dto.MusicBandFormDto;

@Component
public class MusicBandMapper {

    private final CarMapper carMapper;

    public MusicBandMapper(CarMapper carMapper) {
        this.carMapper = carMapper;
    }

    public MusicBandDto toDto(MusicBand band) {
        if (band == null) {
            return null;
        }
        return new MusicBandDto(
                band.getId(),
                band.getName(),
                band.getImpactSpeed(),
                band.getSoundtrackName(),
                band.getMood(),
                band.getCreationDate(),
                carMapper.toDto(band.getCar())
        );
    }

    public void updateEntity(MusicBand band, MusicBandFormDto form, Car car) {
        band.setName(form.getName());
        band.setImpactSpeed(form.getImpactSpeed());
        band.setSoundtrackName(form.getSoundtrackName());
        band.setMood(form.getMood());
        band.setCar(car);
    }
}
