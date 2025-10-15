package ru.ifmo.se.is_lab1.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import ru.ifmo.se.is_lab1.dto.HumanBeingDto;
import ru.ifmo.se.is_lab1.dto.HumanBeingFilter;
import ru.ifmo.se.is_lab1.dto.HumanBeingFormDto;
import ru.ifmo.se.is_lab1.mapper.HumanBeingMapper;
import ru.ifmo.se.is_lab1.domain.Car;
import ru.ifmo.se.is_lab1.domain.Coordinates;
import ru.ifmo.se.is_lab1.domain.HumanBeing;
import ru.ifmo.se.is_lab1.model.Mood;
import ru.ifmo.se.is_lab1.repository.CarRepository;
import ru.ifmo.se.is_lab1.repository.CoordinatesRepository;
import ru.ifmo.se.is_lab1.repository.HumanBeingRepository;
import ru.ifmo.se.is_lab1.repository.HumanBeingSpecifications;
import ru.ifmo.se.is_lab1.service.event.HumanBeingEvent;
import ru.ifmo.se.is_lab1.service.event.HumanBeingEventPublisher;
import ru.ifmo.se.is_lab1.service.event.HumanBeingEventType;

@Service
@Transactional(readOnly = true)
public class HumanBeingService {

    private static final String DEFAULT_CAR_NAME = "Lada Kalina";
    private static final boolean DEFAULT_CAR_COOL = true;

    private final HumanBeingRepository humanBeingRepository;
    private final HumanBeingMapper humanBeingMapper;
    private final CoordinatesRepository coordinatesRepository;
    private final CarService carService;
    private final CarRepository carRepository;
    private final HumanBeingEventPublisher eventPublisher;

    public HumanBeingService(HumanBeingRepository humanBeingRepository,
                             HumanBeingMapper humanBeingMapper,
                             CoordinatesRepository coordinatesRepository,
                             CarService carService,
                             CarRepository carRepository,
                             HumanBeingEventPublisher eventPublisher) {
        this.humanBeingRepository = humanBeingRepository;
        this.humanBeingMapper = humanBeingMapper;
        this.coordinatesRepository = coordinatesRepository;
        this.carService = carService;
        this.carRepository = carRepository;
        this.eventPublisher = eventPublisher;
    }

    public Page<HumanBeingDto> findAll(HumanBeingFilter filter, Pageable pageable) {
        Specification<HumanBeing> specification = HumanBeingSpecifications.withFilter(filter);
        return humanBeingRepository.findAll(specification, pageable)
                .map(humanBeingMapper::toDto);
    }

    public List<HumanBeingDto> findAll() {
        return humanBeingRepository.findAll().stream()
                .map(humanBeingMapper::toDto)
                .collect(Collectors.toList());
    }

    public HumanBeingDto findById(Long id) {
        return humanBeingMapper.toDto(getEntity(id));
    }

    @Transactional
    public HumanBeingDto create(HumanBeingFormDto form) {
        Coordinates coordinates = coordinatesRepository.save(new Coordinates(form.getCoordinatesX(), form.getCoordinatesY()));
        Car car = resolveCar(form.getCarId());
        HumanBeing humanBeing = new HumanBeing(
                form.getName(),
                coordinates,
                form.getRealHero(),
                Boolean.TRUE.equals(form.getHasToothpick()),
                form.getImpactSpeed(),
                form.getSoundtrackName(),
                form.getWeaponType(),
                form.getMood(),
                car
        );
        HumanBeing saved = humanBeingRepository.save(humanBeing);
        HumanBeingDto dto = humanBeingMapper.toDto(saved);
        eventPublisher.publish(new HumanBeingEvent(HumanBeingEventType.CREATED, dto));
        return dto;
    }

    @Transactional
    public HumanBeingDto update(Long id, HumanBeingFormDto form) {
        HumanBeing humanBeing = getEntity(id);
        Coordinates coordinates = humanBeing.getCoordinates();
        if (coordinates == null) {
            coordinates = coordinatesRepository.save(new Coordinates(form.getCoordinatesX(), form.getCoordinatesY()));
        } else {
            coordinates.setX(form.getCoordinatesX());
            coordinates.setY(form.getCoordinatesY());
        }
        Car car = resolveCar(form.getCarId());
        humanBeingMapper.updateEntity(humanBeing, form, coordinates, car);
        HumanBeingDto dto = humanBeingMapper.toDto(humanBeing);
        eventPublisher.publish(new HumanBeingEvent(HumanBeingEventType.UPDATED, dto));
        return dto;
    }

    @Transactional
    public void delete(Long id) {
        HumanBeing humanBeing = getEntity(id);
        HumanBeingDto dto = humanBeingMapper.toDto(humanBeing);
        humanBeingRepository.delete(humanBeing);
        eventPublisher.publish(new HumanBeingEvent(HumanBeingEventType.DELETED, dto));
    }

    public long sumImpactSpeed() {
        Long result = humanBeingRepository.sumImpactSpeed();
        return result != null ? result : 0L;
    }

    public long countByImpactSpeedLessThan(int threshold) {
        Assert.isTrue(threshold > 0, "Порог скорости удара должен быть положительным");
        return humanBeingRepository.countByImpactSpeedLessThan(threshold);
    }

    public List<HumanBeingDto> findBySoundtrackPrefix(String prefix) {
        return humanBeingRepository.findBySoundtrackNameStartingWithIgnoreCase(prefix).stream()
                .map(humanBeingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public int bulkUpdateMood(Mood source, Mood target) {
        int updated = humanBeingRepository.bulkUpdateMood(source, target);
        if (updated > 0) {
            eventPublisher.publish(new HumanBeingEvent(HumanBeingEventType.MOOD_UPDATED, null));
        }
        return updated;
    }

    @Transactional
    public int updateMoodToGloom() {
        int updated = humanBeingRepository.updateMoodForAll(Mood.GLOOM);
        if (updated > 0) {
            eventPublisher.publish(new HumanBeingEvent(HumanBeingEventType.MOOD_UPDATED, null));
        }
        return updated;
    }

    @Transactional
    public int assignDefaultCarToHeroesWithoutCar() {
        if (humanBeingRepository.findByCarIsNull().isEmpty()) {
            return 0;
        }
        Car defaultCar = new Car(DEFAULT_CAR_NAME, DEFAULT_CAR_COOL);
        Car savedCar = carRepository.save(defaultCar);
        int updated = humanBeingRepository.assignCarToAllWithoutCar(savedCar);
        if (updated > 0) {
            eventPublisher.publish(new HumanBeingEvent(HumanBeingEventType.CAR_ASSIGNED, null));
        }
        return updated;
    }

    @Transactional
    public HumanBeingDto assignCar(Long humanId, Long carId) {
        HumanBeing humanBeing = getEntity(humanId);
        Car car = carService.getEntity(carId);
        humanBeing.setCar(car);
        HumanBeingDto dto = humanBeingMapper.toDto(humanBeing);
        eventPublisher.publish(new HumanBeingEvent(HumanBeingEventType.CAR_ASSIGNED, dto));
        return dto;
    }

    private HumanBeing getEntity(Long id) {
        return humanBeingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Человек с указанным идентификатором не найден"));
    }

    private Car resolveCar(Long carId) {
        if (carId == null) {
            return null;
        }
        return carService.getEntity(carId);
    }
}
