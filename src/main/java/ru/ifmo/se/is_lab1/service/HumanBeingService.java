package ru.ifmo.se.is_lab1.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import ru.ifmo.se.is_lab1.dto.HumanBeingDto;
import ru.ifmo.se.is_lab1.dto.HumanBeingFilter;
import ru.ifmo.se.is_lab1.dto.HumanBeingFormDto;
import ru.ifmo.se.is_lab1.dto.HumanBeingSummary;
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
import ru.ifmo.se.is_lab1.service.exception.HumanBeingDeletionException;
import ru.ifmo.se.is_lab1.service.exception.HumanBeingUniquenessException;

/**
 * Сервис работы с людьми. Дополнительные бизнес-ограничения уникальности:
 * <ul>
 *     <li>Комбинация имени и названия саундтрека уникальна (без учёта регистра).</li>
 *     <li>Для настоящих героев (realHero = true) скорость удара уникальна.</li>
 * </ul>
 */
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

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public HumanBeingDto create(HumanBeingFormDto form) {
        ensureUniqueConstraints(null, form);
        Coordinates coordinates = coordinatesRepository.save(new Coordinates(form.getCoordinatesX(), form.getCoordinatesY()));
        Car car = resolveCar(form.getCarId());
        String name = form.getName() != null ? form.getName().trim() : null;
        String soundtrackName = form.getSoundtrackName() != null ? form.getSoundtrackName().trim() : null;
        HumanBeing humanBeing = new HumanBeing(
                name,
                coordinates,
                form.getRealHero(),
                Boolean.TRUE.equals(form.getHasToothpick()),
                form.getImpactSpeed(),
                soundtrackName,
                form.getWeaponType(),
                form.getMood(),
                car
        );
        try {
            HumanBeing saved = humanBeingRepository.saveAndFlush(humanBeing);
            HumanBeingDto dto = humanBeingMapper.toDto(saved);
            publishChange(new HumanBeingEvent(HumanBeingEventType.CREATED, dto));
            return dto;
        } catch (DataIntegrityViolationException ex) {
            throw HumanBeingUniquenessResolver.translate(ex);
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public HumanBeingDto update(Long id, HumanBeingFormDto form) {
        HumanBeing humanBeing = getEntity(id);
        ensureUniqueConstraints(id, form);
        Coordinates coordinates = humanBeing.getCoordinates();
        if (coordinates == null) {
            coordinates = coordinatesRepository.save(new Coordinates(form.getCoordinatesX(), form.getCoordinatesY()));
        } else {
            coordinates.setX(form.getCoordinatesX());
            coordinates.setY(form.getCoordinatesY());
        }
        Car car = resolveCar(form.getCarId());
        humanBeingMapper.updateEntity(humanBeing, form, coordinates, car);
        try {
            humanBeingRepository.saveAndFlush(humanBeing);
            HumanBeingDto dto = humanBeingMapper.toDto(humanBeing);
            publishChange(new HumanBeingEvent(HumanBeingEventType.UPDATED, dto));
            return dto;
        } catch (DataIntegrityViolationException ex) {
            throw HumanBeingUniquenessResolver.translate(ex);
        }
    }

    @Transactional
    public void delete(Long id) {
        HumanBeing humanBeing = humanBeingRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException("Человек с указанным идентификатором не найден"));
        if (humanBeing.getCar() != null) {
            throw new HumanBeingDeletionException("Невозможно удалить человека: к нему привязан автомобиль");
        }
        HumanBeingDto dto = humanBeingMapper.toDto(humanBeing);
        humanBeingRepository.delete(humanBeing);
        publishChange(new HumanBeingEvent(HumanBeingEventType.DELETED, dto));
    }

    public long sumImpactSpeed() {
        Long result = humanBeingRepository.sumImpactSpeed();
        return result != null ? result : 0L;
    }

    public HumanBeingSummary getSummary() {
        long totalCount = humanBeingRepository.count();
        long totalImpactSpeed = sumImpactSpeed();
        return new HumanBeingSummary(totalCount, totalImpactSpeed);
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
            publishChange(new HumanBeingEvent(HumanBeingEventType.MOOD_UPDATED, null));
        }
        return updated;
    }

    @Transactional
    public int updateMoodToGloom() {
        int updated = humanBeingRepository.updateMoodForAll(Mood.GLOOM);
        if (updated > 0) {
            publishChange(new HumanBeingEvent(HumanBeingEventType.MOOD_UPDATED, null));
        }
        return updated;
    }

    @Transactional
    public int assignDefaultCarToHeroesWithoutCar() {
        if (humanBeingRepository.findByCarIsNullForUpdate().isEmpty()) {
            return 0;
        }
        Car defaultCar = new Car(DEFAULT_CAR_NAME, DEFAULT_CAR_COOL);
        Car savedCar = carRepository.save(defaultCar);
        int updated = humanBeingRepository.assignCarToAllWithoutCar(savedCar);
        if (updated > 0) {
            publishChange(new HumanBeingEvent(HumanBeingEventType.CAR_ASSIGNED, null));
        }
        return updated;
    }

    @Transactional
    public HumanBeingDto assignCar(Long humanId, Long carId) {
        HumanBeing humanBeing = humanBeingRepository.findByIdForUpdate(humanId)
                .orElseThrow(() -> new IllegalArgumentException("Человек с указанным идентификатором не найден"));
        Car car = carService.getEntity(carId);
        humanBeing.setCar(car);
        HumanBeingDto dto = humanBeingMapper.toDto(humanBeing);
        publishChange(new HumanBeingEvent(HumanBeingEventType.CAR_ASSIGNED, dto));
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

    private void ensureUniqueConstraints(Long currentId, HumanBeingFormDto form) {
        ensureUniqueConstraints(currentId,
                form.getName(),
                form.getSoundtrackName(),
                form.getRealHero(),
                form.getImpactSpeed());
    }

    public void ensureUniqueConstraints(Long currentId,
                                        String name,
                                        String soundtrack,
                                        Boolean realHero,
                                        Integer impactSpeed) {
        String trimmedName = name != null ? name.trim() : null;
        String trimmedSoundtrack = soundtrack != null ? soundtrack.trim() : null;
        if (trimmedName != null && trimmedSoundtrack != null
                && humanBeingRepository.hasNameAndSoundtrackConflict(trimmedName, trimmedSoundtrack, currentId)) {
            throw new HumanBeingUniquenessException("Комбинация имени и саундтрека уже используется другим человеком");
        }
        boolean isRealHero = Boolean.TRUE.equals(realHero);
        if (isRealHero && impactSpeed != null
                && humanBeingRepository.hasRealHeroImpactSpeedConflict(impactSpeed, currentId)) {
            throw new HumanBeingUniquenessException("Скорость удара настоящего героя должна быть уникальной");
        }
    }

    private void publishChange(HumanBeingEvent event) {
        Runnable publisher = () -> {
            eventPublisher.publish(event);
            eventPublisher.publishSummary(getSummary());
        };

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publisher.run();
                }
            });
        } else {
            publisher.run();
        }
    }
}
