package ru.ifmo.se.is_lab1.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.ifmo.se.is_lab1.dto.CarOptionDto;
import ru.ifmo.se.is_lab1.model.Car;
import ru.ifmo.se.is_lab1.repository.LegacyCarRepository;

@Service
@Transactional(readOnly = true)
public class CarLookupService {

    private final LegacyCarRepository carRepository;

    public CarLookupService(LegacyCarRepository carRepository) {
        this.carRepository = carRepository;
    }

    public List<CarOptionDto> findAll() {
        return carRepository.findAll().stream()
                .map(car -> new CarOptionDto(car.getId(), car.getName(), car.getCool()))
                .collect(Collectors.toList());
    }

    public Car getEntity(Long id) {
        return carRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Автомобиль не найден"));
    }
}
