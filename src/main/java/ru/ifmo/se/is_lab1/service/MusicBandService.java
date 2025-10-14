package ru.ifmo.se.is_lab1.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.se.is_lab1.domain.Car;
import ru.ifmo.se.is_lab1.domain.Mood;
import ru.ifmo.se.is_lab1.domain.MusicBand;
import ru.ifmo.se.is_lab1.dto.MusicBandDto;
import ru.ifmo.se.is_lab1.dto.MusicBandFilter;
import ru.ifmo.se.is_lab1.dto.MusicBandFormDto;
import ru.ifmo.se.is_lab1.mapper.MusicBandMapper;
import ru.ifmo.se.is_lab1.repository.MusicBandRepository;
import ru.ifmo.se.is_lab1.repository.MusicBandSpecifications;
import ru.ifmo.se.is_lab1.service.event.MusicBandEvent;
import ru.ifmo.se.is_lab1.service.event.MusicBandEventPublisher;
import ru.ifmo.se.is_lab1.service.event.MusicBandEventType;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MusicBandService {

    private final MusicBandRepository musicBandRepository;
    private final MusicBandMapper musicBandMapper;
    private final CarService carService;
    private final MusicBandEventPublisher eventPublisher;

    public MusicBandService(MusicBandRepository musicBandRepository,
                            MusicBandMapper musicBandMapper,
                            CarService carService,
                            MusicBandEventPublisher eventPublisher) {
        this.musicBandRepository = musicBandRepository;
        this.musicBandMapper = musicBandMapper;
        this.carService = carService;
        this.eventPublisher = eventPublisher;
    }

    public Page<MusicBandDto> findAll(MusicBandFilter filter, Pageable pageable) {
        Specification<MusicBand> specification = MusicBandSpecifications.withFilter(filter);
        return musicBandRepository.findAll(specification, pageable)
                .map(musicBandMapper::toDto);
    }

    public MusicBandDto findById(Long id) {
        return musicBandMapper.toDto(getEntity(id));
    }

    @Transactional
    public MusicBandDto create(MusicBandFormDto form) {
        MusicBand band = new MusicBand();
        Car car = resolveCar(form.getCarId());
        musicBandMapper.updateEntity(band, form, car);
        MusicBand saved = musicBandRepository.save(band);
        MusicBandDto dto = musicBandMapper.toDto(saved);
        eventPublisher.publish(new MusicBandEvent(MusicBandEventType.CREATED, dto));
        return dto;
    }

    @Transactional
    public MusicBandDto update(Long id, MusicBandFormDto form) {
        MusicBand band = getEntity(id);
        Car car = resolveCar(form.getCarId());
        musicBandMapper.updateEntity(band, form, car);
        MusicBandDto dto = musicBandMapper.toDto(band);
        eventPublisher.publish(new MusicBandEvent(MusicBandEventType.UPDATED, dto));
        return dto;
    }

    @Transactional
    public void delete(Long id) {
        MusicBand band = getEntity(id);
        MusicBandDto dto = musicBandMapper.toDto(band);
        musicBandRepository.delete(band);
        eventPublisher.publish(new MusicBandEvent(MusicBandEventType.DELETED, dto));
    }

    public BigDecimal sumImpactSpeed() {
        return musicBandRepository.sumImpactSpeed();
    }

    public List<MusicBandDto> findBySoundtrackPrefix(String prefix) {
        return musicBandRepository.findBySoundtrackNameStartingWithIgnoreCase(prefix).stream()
                .map(musicBandMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public int bulkUpdateMood(Mood source, Mood target) {
        int updated = musicBandRepository.bulkUpdateMood(source, target);
        if (updated > 0) {
            eventPublisher.publish(new MusicBandEvent(MusicBandEventType.MOOD_UPDATED, null));
        }
        return updated;
    }

    @Transactional
    public MusicBandDto assignCar(Long bandId, Long carId) {
        MusicBand band = getEntity(bandId);
        Car car = carService.getEntity(carId);
        band.setCar(car);
        MusicBandDto dto = musicBandMapper.toDto(band);
        eventPublisher.publish(new MusicBandEvent(MusicBandEventType.CAR_ASSIGNED, dto));
        return dto;
    }

    public List<MusicBandDto> findAll() {
        return musicBandRepository.findAll().stream()
                .map(musicBandMapper::toDto)
                .collect(Collectors.toList());
    }

    private MusicBand getEntity(Long id) {
        return musicBandRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Музыкальная группа не найдена"));
    }

    private Car resolveCar(Long carId) {
        if (carId == null) {
            return null;
        }
        return carService.getEntity(carId);
    }
}
