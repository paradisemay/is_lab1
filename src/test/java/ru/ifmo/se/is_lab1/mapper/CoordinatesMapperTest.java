package ru.ifmo.se.is_lab1.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import ru.ifmo.se.is_lab1.domain.Coordinates;
import ru.ifmo.se.is_lab1.dto.CoordinatesDto;

import static org.assertj.core.api.Assertions.assertThat;

class CoordinatesMapperTest {

    private final CoordinatesMapper mapper = new CoordinatesMapper();

    @Test
    void toDtoShouldReturnNullWhenEntityIsNull() {
        assertThat(mapper.toDto(null)).isNull();
    }

    @Test
    void toDtoShouldMapAllFields() {
        Coordinates coordinates = new Coordinates(10, 5.5f);
        ReflectionTestUtils.setField(coordinates, "id", 7L);

        CoordinatesDto dto = mapper.toDto(coordinates);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(7L);
        assertThat(dto.getX()).isEqualTo(10);
        assertThat(dto.getY()).isEqualTo(5.5f);
    }
}
