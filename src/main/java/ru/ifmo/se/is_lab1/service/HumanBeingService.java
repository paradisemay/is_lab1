package ru.ifmo.se.is_lab1.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.ifmo.se.is_lab1.dto.HumanBeingDto;
import ru.ifmo.se.is_lab1.dto.HumanBeingFilter;
import ru.ifmo.se.is_lab1.dto.HumanBeingFormDto;
import ru.ifmo.se.is_lab1.mapper.HumanBeingMapper;
import ru.ifmo.se.is_lab1.model.Car;
import ru.ifmo.se.is_lab1.model.Coordinates;
import ru.ifmo.se.is_lab1.model.HumanBeing;
import ru.ifmo.se.is_lab1.model.Mood;
import ru.ifmo.se.is_lab1.repository.CoordinatesRepository;
import ru.ifmo.se.is_lab1.repository.HumanBeingRepository;
import ru.ifmo.se.is_lab1.repository.HumanBeingSpecifications;
import ru.ifmo.se.is_lab1.service.event.HumanBeingEvent;
import ru.ifmo.se.is_lab1.service.event.HumanBeingEventPublisher;
import ru.ifmo.se.is_lab1.service.event.HumanBeingEventType;

@Service
@Transactional(readOnly = true)
public class HumanBeingService {

    private final HumanBeingRepository humanBeingRepository;
    private final CoordinatesRepository coordinatesRepository;
    private final CarLookupService carLookupService;
    private final HumanBeingMapper humanBeingMapper;
    private final HumanBeingEventPublisher eventPublisher;

    public HumanBeingService(HumanBeingRepository humanBeingRepository,
                             CoordinatesRepository coordinatesRepository,
                             CarLookupService carLookupService,
                             HumanBeingMapper humanBeingMapper,
                             HumanBeingEventPublisher eventPublisher) {
        this.humanBeingRepository = humanBeingRepository;
        this.coordinatesRepository = coordinatesRepository;
        this.carLookupService = carLookupService;
        this.humanBeingMapper = humanBeingMapper;
        this.eventPublisher = eventPublisher;
    }

    public Page<HumanBeingDto> findAll(HumanBeingFilter filter, Pageable pageable) {
        Specification<HumanBeing> specification = HumanBeingSpecifications.withFilter(filter);
        return humanBeingRepository.findAll(specification, pageable)
                .map(humanBeingMapper::toDto);
    }

    public HumanBeingDto findById(Long id) {
        return humanBeingMapper.toDto(getEntity(id));
    }

    @Transactional
    public HumanBeingDto create(HumanBeingFormDto form) {
        HumanBeing human = new HumanBeing(
                form.getName(),
                saveCoordinates(null, form),
                form.isRealHero(),
                form.getHasToothpick(),
                form.getImpactSpeed(),
                form.getSoundtrackName(),
                form.getMinutesOfWaiting(),
                form.getWeaponType(),
                form.getMood(),
                resolveCar(form.getCarId())
        );
        HumanBeing saved = humanBeingRepository.save(human);
        HumanBeingDto dto = humanBeingMapper.toDto(saved);
        eventPublisher.publish(new HumanBeingEvent(HumanBeingEventType.CREATED, dto));
        return dto;
    }

    @Transactional
    public HumanBeingDto update(Long id, HumanBeingFormDto form) {
        HumanBeing human = getEntity(id);
        human.setName(form.getName());
        human.setRealHero(form.isRealHero());
        human.setHasToothpick(form.getHasToothpick());
        human.setImpactSpeed(form.getImpactSpeed());
        human.setSoundtrackName(form.getSoundtrackName());
        human.setMinutesOfWaiting(form.getMinutesOfWaiting());
        human.setWeaponType(form.getWeaponType());
        human.setMood(form.getMood());
        human.setCar(resolveCar(form.getCarId()));
        human.setCoordinates(saveCoordinates(human.getCoordinates(), form));
        HumanBeingDto dto = humanBeingMapper.toDto(human);
        eventPublisher.publish(new HumanBeingEvent(HumanBeingEventType.UPDATED, dto));
        return dto;
    }

    @Transactional
    public void delete(Long id) {
        HumanBeing human = getEntity(id);
        HumanBeingDto dto = humanBeingMapper.toDto(human);
        humanBeingRepository.delete(human);
        eventPublisher.publish(new HumanBeingEvent(HumanBeingEventType.DELETED, dto));
    }

    public double sumImpactSpeed() {
        return humanBeingRepository.sumImpactSpeed();
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
    public HumanBeingDto assignCar(Long id, Long carId) {
        HumanBeing human = getEntity(id);
        human.setCar(carLookupService.getEntity(carId));
        HumanBeingDto dto = humanBeingMapper.toDto(human);
        eventPublisher.publish(new HumanBeingEvent(HumanBeingEventType.CAR_ASSIGNED, dto));
        return dto;
    }

    public List<HumanBeingDto> findAll() {
        return humanBeingRepository.findAll().stream()
                .map(humanBeingMapper::toDto)
                .collect(Collectors.toList());
    }

    private HumanBeing getEntity(Long id) {
        return humanBeingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Человек не найден"));
    }

    private Car resolveCar(Long carId) {
        if (carId == null) {
            return null;
        }
        return carLookupService.getEntity(carId);
    }

    private Coordinates saveCoordinates(Coordinates existing, HumanBeingFormDto form) {
        Coordinates coordinates = existing;
        if (coordinates == null) {
            coordinates = new Coordinates(form.getCoordinateX(), form.getCoordinateY());
        } else {
            coordinates.setX(form.getCoordinateX());
            coordinates.setY(form.getCoordinateY());
        }
        return coordinatesRepository.save(coordinates);
    }
}
