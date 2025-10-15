package ru.ifmo.se.is_lab1.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.ifmo.se.is_lab1.domain.Car;
import ru.ifmo.se.is_lab1.dto.CarDto;
import ru.ifmo.se.is_lab1.mapper.CarMapper;
import ru.ifmo.se.is_lab1.repository.CarRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarServiceTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private CarMapper carMapper;

    @InjectMocks
    private CarService carService;

    private Car car;
    private CarDto carDto;

    @BeforeEach
    void setUp() {
        car = new Car("Lada", true);
        carDto = new CarDto(1L, "Lada", true);
    }

    @Test
    void findAllShouldReturnMappedCars() {
        when(carRepository.findAll()).thenReturn(List.of(car));
        when(carMapper.toDto(car)).thenReturn(carDto);

        List<CarDto> result = carService.findAll();

        assertThat(result).containsExactly(carDto);
    }

    @Test
    void getEntityShouldReturnCarWhenExists() {
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));

        Car result = carService.getEntity(1L);

        assertThat(result).isSameAs(car);
    }

    @Test
    void getEntityShouldThrowWhenCarMissing() {
        when(carRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carService.getEntity(2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Автомобиль не найден");
    }

    @Test
    void createShouldPersistAndMapNewCar() {
        when(carRepository.save(any(Car.class))).thenAnswer(invocation -> {
            Car saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 9L);
            return saved;
        });
        when(carMapper.toDto(any(Car.class))).thenReturn(carDto);

        CarDto result = carService.create("Tesla", false);

        ArgumentCaptor<Car> carCaptor = ArgumentCaptor.forClass(Car.class);
        verify(carRepository).save(carCaptor.capture());
        Car saved = carCaptor.getValue();
        assertThat(saved.getName()).isEqualTo("Tesla");
        assertThat(saved.getCool()).isFalse();
        assertThat(result).isSameAs(carDto);
    }

    @Test
    void updateShouldModifyExistingCar() {
        Car existing = new Car("Old", false);
        when(carRepository.findById(1L)).thenReturn(Optional.of(existing));
        CarDto updatedDto = new CarDto(1L, "New", true);
        when(carMapper.toDto(existing)).thenReturn(updatedDto);

        CarDto result = carService.update(1L, "New", true);

        assertThat(existing.getName()).isEqualTo("New");
        assertThat(existing.getCool()).isTrue();
        assertThat(result.getName()).isEqualTo("New");
        assertThat(result.getCool()).isTrue();
    }

    @Test
    void deleteShouldDelegateToRepository() {
        carService.delete(3L);

        verify(carRepository).deleteById(3L);
    }
}
