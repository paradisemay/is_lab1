package ru.ifmo.se.is_lab1.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.se.is_lab1.dto.CarDto;
import ru.ifmo.se.is_lab1.mapper.CarMapper;
import ru.ifmo.se.is_lab1.repository.CarRepository;
import ru.ifmo.se.is_lab1.domain.Car;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CarService {

    private final CarRepository carRepository;
    private final CarMapper carMapper;

    public CarService(CarRepository carRepository, CarMapper carMapper) {
        this.carRepository = carRepository;
        this.carMapper = carMapper;
    }

    public List<CarDto> findAll() {
        return carRepository.findAll().stream()
                .map(carMapper::toDto)
                .collect(Collectors.toList());
    }

    public Car getEntity(Long id) {
        return carRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Автомобиль не найден"));
    }

    @Transactional
    public CarDto create(String name, Boolean cool) {
        Car car = new Car(name, cool);
        return carMapper.toDto(carRepository.save(car));
    }

    @Transactional
    public CarDto update(Long id, String name, Boolean cool) {
        Car car = getEntity(id);
        car.setName(name);
        car.setCool(cool);
        return carMapper.toDto(car);
    }

    @Transactional
    public void delete(Long id) {
        carRepository.deleteById(id);
    }
}
