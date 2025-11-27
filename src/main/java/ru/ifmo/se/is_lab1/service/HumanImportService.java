package ru.ifmo.se.is_lab1.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.springframework.dao.DataIntegrityViolationException;
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
import ru.ifmo.se.is_lab1.service.ImportOperationService;
import ru.ifmo.se.is_lab1.service.HumanBeingService;
import ru.ifmo.se.is_lab1.service.event.HumanBeingEvent;
import ru.ifmo.se.is_lab1.service.event.HumanBeingEventPublisher;
import ru.ifmo.se.is_lab1.service.event.HumanBeingEventType;
import ru.ifmo.se.is_lab1.service.exception.HumanBeingUniquenessException;
import ru.ifmo.se.is_lab1.service.exception.HumanImportException;

@Service
public class HumanImportService {

    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final CoordinatesRepository coordinatesRepository;
    private final CarRepository carRepository;
    private final HumanBeingRepository humanBeingRepository;
    private final HumanBeingEventPublisher eventPublisher;
    private final ImportOperationService importOperationService;
    private final HumanBeingService humanBeingService;

    public HumanImportService(ObjectMapper objectMapper,
                              Validator validator,
                              CoordinatesRepository coordinatesRepository,
                              CarRepository carRepository,
                              HumanBeingRepository humanBeingRepository,
                              HumanBeingEventPublisher eventPublisher,
                              ImportOperationService importOperationService,
                              HumanBeingService humanBeingService) {
        this.objectMapper = objectMapper;
        this.validator = validator;
        this.coordinatesRepository = coordinatesRepository;
        this.carRepository = carRepository;
        this.humanBeingRepository = humanBeingRepository;
        this.eventPublisher = eventPublisher;
        this.importOperationService = importOperationService;
        this.humanBeingService = humanBeingService;
    }

    @Transactional
    public int importHumans(MultipartFile file) {
        var operation = importOperationService.startOperation();
        try {
            List<HumanImportRecordDto> records = readRecords(file);
            if (records.isEmpty()) {
                throw new HumanImportException("Файл не содержит данных для импорта");
            }
            validateRecords(records);

            Map<String, Car> carCache = new HashMap<>();
            List<HumanBeing> humansToSave = new ArrayList<>();
            Set<String> nameSoundtrackInBatch = new HashSet<>();
            Set<Integer> realHeroImpactInBatch = new HashSet<>();
            for (int i = 0; i < records.size(); i++) {
                HumanImportRecordDto record = records.get(i);
                String trimmedName = record.getName() != null ? record.getName().trim() : null;
                String trimmedSoundtrack = record.getSoundtrackName() != null ? record.getSoundtrackName().trim() : null;
                if (trimmedName != null && trimmedSoundtrack != null) {
                    String key = trimmedName + "|" + trimmedSoundtrack;
                    if (!nameSoundtrackInBatch.add(key)) {
                        throw new HumanImportException(String.format(
                                "Запись #%d: комбинация имени '%s' и саундтрека '%s' повторяется в импортируемом файле",
                                i + 1,
                                trimmedName,
                                trimmedSoundtrack));
                    }
                }
                boolean isRealHero = Boolean.TRUE.equals(record.getRealHero());
                Integer impactSpeed = record.getImpactSpeed();
                if (isRealHero && impactSpeed != null) {
                    if (!realHeroImpactInBatch.add(impactSpeed)) {
                        throw new HumanImportException(String.format(
                                "Запись #%d: скорость удара настоящего героя %d повторяется в импортируемом файле",
                                i + 1,
                                impactSpeed));
                    }
                }
                try {
                    humanBeingService.ensureUniqueConstraints(null,
                            trimmedName,
                            trimmedSoundtrack,
                            record.getRealHero(),
                            impactSpeed);
                } catch (HumanBeingUniquenessException ex) {
                    throw new HumanImportException(String.format("Запись #%d: %s", i + 1, ex.getMessage()), ex);
                }

                Coordinates coordinates = coordinatesRepository.save(new Coordinates(
                        record.getCoordinates().getX(),
                        record.getCoordinates().getY()
                ));
                Car car = resolveCar(record.getCar(), carCache);
                if (car == null) {
                    throw new HumanImportException("Не удалось восстановить информацию об автомобиле");
                }
                HumanBeing humanBeing = new HumanBeing(
                        trimmedName,
                        coordinates,
                        record.getRealHero(),
                        Boolean.TRUE.equals(record.getHasToothpick()),
                        impactSpeed,
                        trimmedSoundtrack,
                        record.getWeaponType(),
                        record.getMood(),
                        car
                );
                humansToSave.add(humanBeing);
            }
            List<HumanBeing> saved = humanBeingRepository.saveAll(humansToSave);
            humanBeingRepository.flush();
            int imported = saved.size();
            importOperationService.markSuccess(operation.getId(), imported);
            publishAfterCommit();
            return imported;
        } catch (DataIntegrityViolationException e) {
            HumanBeingUniquenessException translated = HumanBeingUniquenessResolver.translate(e);
            importOperationService.markFailure(operation.getId(), resolveErrorMessage(translated));
            throw translated;
        } catch (RuntimeException e) {
            importOperationService.markFailure(operation.getId(), resolveErrorMessage(e));
            throw e;
        }
    }

    private String resolveErrorMessage(RuntimeException exception) {
        if (exception == null) {
            return "Неизвестная ошибка";
        }
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            message = exception.getClass().getSimpleName();
        }
        if (message.length() > 1000) {
            return message.substring(0, 1000);
        }
        return message;
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
