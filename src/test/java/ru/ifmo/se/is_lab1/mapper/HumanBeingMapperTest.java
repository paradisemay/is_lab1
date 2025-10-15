package ru.ifmo.se.is_lab1.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import ru.ifmo.se.is_lab1.domain.Car;
import ru.ifmo.se.is_lab1.domain.Coordinates;
import ru.ifmo.se.is_lab1.domain.HumanBeing;
import ru.ifmo.se.is_lab1.dto.CarDto;
import ru.ifmo.se.is_lab1.dto.CoordinatesDto;
import ru.ifmo.se.is_lab1.dto.HumanBeingDto;
import ru.ifmo.se.is_lab1.dto.HumanBeingFormDto;
import ru.ifmo.se.is_lab1.model.Mood;
import ru.ifmo.se.is_lab1.model.WeaponType;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class HumanBeingMapperTest {

    private final HumanBeingMapper mapper = new HumanBeingMapper(new CarMapper(), new CoordinatesMapper());

    @Test
    void toDtoShouldReturnNullWhenEntityIsNull() {
        assertThat(mapper.toDto(null)).isNull();
    }

    @Test
    void toDtoShouldMapAllFields() {
        Coordinates coordinates = new Coordinates(10, 4.2f);
        ReflectionTestUtils.setField(coordinates, "id", 11L);
        Car car = new Car("Volga", false);
        ReflectionTestUtils.setField(car, "id", 5L);
        HumanBeing human = new HumanBeing("Ivan", coordinates, true, true, 300, "Song", WeaponType.SHOTGUN, Mood.GLOOM, car);
        ReflectionTestUtils.setField(human, "id", 1L);
        ReflectionTestUtils.setField(human, "creationDate", Instant.parse("2024-01-01T00:00:00Z"));

        HumanBeingDto dto = mapper.toDto(human);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Ivan");
        CoordinatesDto coordinatesDto = dto.getCoordinates();
        assertThat(coordinatesDto.getId()).isEqualTo(11L);
        assertThat(coordinatesDto.getX()).isEqualTo(10);
        assertThat(coordinatesDto.getY()).isEqualTo(4.2f);
        assertThat(dto.getCreationDate()).isEqualTo(Instant.parse("2024-01-01T00:00:00Z"));
        assertThat(dto.getRealHero()).isTrue();
        assertThat(dto.isHasToothpick()).isTrue();
        assertThat(dto.getImpactSpeed()).isEqualTo(300);
        assertThat(dto.getSoundtrackName()).isEqualTo("Song");
        assertThat(dto.getWeaponType()).isEqualTo(WeaponType.SHOTGUN);
        assertThat(dto.getMood()).isEqualTo(Mood.GLOOM);
        CarDto carDto = dto.getCar();
        assertThat(carDto.getId()).isEqualTo(5L);
        assertThat(carDto.getName()).isEqualTo("Volga");
        assertThat(carDto.getCool()).isFalse();
    }

    @Test
    void updateEntityShouldCopyAllFieldsFromForm() {
        Coordinates originalCoordinates = new Coordinates(0, 0f);
        Car originalCar = new Car("Old", false);
        HumanBeing human = new HumanBeing("OldName", originalCoordinates, false, false, 100, "OldSong", WeaponType.BAT, Mood.SADNESS, originalCar);

        HumanBeingFormDto form = new HumanBeingFormDto();
        form.setName("New");
        form.setCoordinatesX(1);
        form.setCoordinatesY(2.5f);
        form.setRealHero(true);
        form.setHasToothpick(false);
        form.setImpactSpeed(200);
        form.setSoundtrackName("NewSong");
        form.setWeaponType(WeaponType.MACHINE_GUN);
        form.setMood(Mood.LONGING);
        form.setCarId(3L);

        Coordinates newCoordinates = new Coordinates(1, 2.5f);
        Car newCar = new Car("NewCar", true);

        mapper.updateEntity(human, form, newCoordinates, newCar);

        assertThat(human.getName()).isEqualTo("New");
        assertThat(human.getCoordinates()).isSameAs(newCoordinates);
        assertThat(human.getRealHero()).isTrue();
        assertThat(human.getHasToothpick()).isFalse();
        assertThat(human.getImpactSpeed()).isEqualTo(200);
        assertThat(human.getSoundtrackName()).isEqualTo("NewSong");
        assertThat(human.getWeaponType()).isEqualTo(WeaponType.MACHINE_GUN);
        assertThat(human.getMood()).isEqualTo(Mood.LONGING);
        assertThat(human.getCar()).isSameAs(newCar);
    }
}
