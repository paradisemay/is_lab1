package ru.ifmo.se.is_lab1.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import ru.ifmo.se.is_lab1.domain.Car;
import ru.ifmo.se.is_lab1.domain.Coordinates;
import ru.ifmo.se.is_lab1.domain.HumanBeing;
import ru.ifmo.se.is_lab1.dto.HumanBeingSummary;
import ru.ifmo.se.is_lab1.dto.HumanImportRecordDto;
import ru.ifmo.se.is_lab1.repository.CarRepository;
import ru.ifmo.se.is_lab1.repository.CoordinatesRepository;
import ru.ifmo.se.is_lab1.repository.HumanBeingRepository;
import ru.ifmo.se.is_lab1.service.event.HumanBeingEvent;
import ru.ifmo.se.is_lab1.service.event.HumanBeingEventPublisher;
import ru.ifmo.se.is_lab1.service.event.HumanBeingEventType;
import ru.ifmo.se.is_lab1.service.exception.HumanImportException;

@Service
public class HumanImportService {

    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final CoordinatesRepository coordinatesRepository;
    private final CarRepository carRepository;
    private final HumanBeingRepository humanBeingRepository;
    private final HumanBeingEventPublisher eventPublisher;

    public HumanImportService(ObjectMapper objectMapper,
                              Validator validator,
                              CoordinatesRepository coordinatesRepository,
                              CarRepository carRepository,
                              HumanBeingRepository humanBeingRepository,
                              HumanBeingEventPublisher eventPublisher) {
        this.objectMapper = objectMapper;
        this.validator = validator;
        this.coordinatesRepository = coordinatesRepository;
        this.carRepository = carRepository;
        this.humanBeingRepository = humanBeingRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public int importHumans(MultipartFile file) {
        List<HumanImportRecordDto> records = readRecords(file);
        if (records.isEmpty()) {
            throw new HumanImportException("Файл не содержит данных для импорта");
        }
        validateRecords(records);

        Map<String, Car> carCache = new HashMap<>();
        List<HumanBeing> humansToSave = new ArrayList<>();
        for (HumanImportRecordDto record : records) {
            Coordinates coordinates = coordinatesRepository.save(new Coordinates(
                    record.getCoordinates().getX(),
                    record.getCoordinates().getY()
            ));
            Car car = resolveCar(record.getCar(), carCache);
            HumanBeing humanBeing = new HumanBeing(
                    record.getName().trim(),
                    coordinates,
                    record.getRealHero(),
                    Boolean.TRUE.equals(record.getHasToothpick()),
                    record.getImpactSpeed(),
                    record.getSoundtrackName().trim(),
                    record.getWeaponType(),
                    record.getMood(),
                    car
            );
            humansToSave.add(humanBeing);
        }
        List<HumanBeing> saved = humanBeingRepository.saveAll(humansToSave);
        publishAfterCommit();
        return saved.size();
    }

    private List<HumanImportRecordDto> readRecords(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new HumanImportException("Файл импорта не должен быть пустым");
        }
        try (InputStream inputStream = file.getInputStream()) {
            return objectMapper.readValue(inputStream, new TypeReference<List<HumanImportRecordDto>>() {
            });
        } catch (IOException e) {
            throw new HumanImportException("Не удалось прочитать JSON: " + e.getMessage(), e);
        }
    }

    private void validateRecords(List<HumanImportRecordDto> records) {
        List<String> errors = new ArrayList<>();
        for (int i = 0; i < records.size(); i++) {
            HumanImportRecordDto record = records.get(i);
            Set<ConstraintViolation<HumanImportRecordDto>> recordViolations = validator.validate(record);
            if (!recordViolations.isEmpty()) {
                String message = recordViolations.stream()
                        .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                        .collect(Collectors.joining(", "));
                errors.add(String.format("Запись #%d — %s", i + 1, message));
            }
        }
        if (!errors.isEmpty()) {
            throw new HumanImportException(String.join("; ", errors));
        }
    }

    private Car resolveCar(HumanImportRecordDto.Car carDto, Map<String, Car> cache) {
        if (carDto == null) {
            return null;
        }
        String key = carDto.getName().trim().toLowerCase();
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        Optional<Car> existing = carRepository.findByNameIgnoreCase(carDto.getName().trim());
        Car car = existing.orElseGet(() -> carRepository.save(new Car(carDto.getName().trim(), carDto.getCool())));
        cache.put(key, car);
        return car;
    }

    private void publishAfterCommit() {
        Runnable publisher = () -> {
            eventPublisher.publish(new HumanBeingEvent(HumanBeingEventType.CREATED, null));
            eventPublisher.publishSummary(buildSummary());
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

    private HumanBeingSummary buildSummary() {
        long totalCount = humanBeingRepository.count();
        long totalImpact = Optional.ofNullable(humanBeingRepository.sumImpactSpeed()).orElse(0L);
        return new HumanBeingSummary(totalCount, totalImpact);
    }
}
