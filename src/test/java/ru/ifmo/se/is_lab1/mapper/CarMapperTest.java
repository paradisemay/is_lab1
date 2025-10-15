package ru.ifmo.se.is_lab1.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import ru.ifmo.se.is_lab1.domain.Car;
import ru.ifmo.se.is_lab1.dto.CarDto;

import static org.assertj.core.api.Assertions.assertThat;

class CarMapperTest {

    private final CarMapper mapper = new CarMapper();

    @Test
    void toDtoShouldReturnNullWhenEntityIsNull() {
        assertThat(mapper.toDto(null)).isNull();
    }

    @Test
    void toDtoShouldMapAllFields() {
        Car car = new Car("Tesla", true);
        ReflectionTestUtils.setField(car, "id", 42L);

        CarDto dto = mapper.toDto(car);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(42L);
        assertThat(dto.getName()).isEqualTo("Tesla");
        assertThat(dto.getCool()).isTrue();
    }
}
