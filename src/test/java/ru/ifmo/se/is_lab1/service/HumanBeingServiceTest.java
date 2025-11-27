package ru.ifmo.se.is_lab1.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import ru.ifmo.se.is_lab1.domain.Car;
import ru.ifmo.se.is_lab1.domain.Coordinates;
import ru.ifmo.se.is_lab1.domain.HumanBeing;
import ru.ifmo.se.is_lab1.dto.HumanBeingDto;
import ru.ifmo.se.is_lab1.dto.HumanBeingFilter;
import ru.ifmo.se.is_lab1.dto.HumanBeingFormDto;
import ru.ifmo.se.is_lab1.dto.HumanBeingSummary;
import ru.ifmo.se.is_lab1.mapper.HumanBeingMapper;
import ru.ifmo.se.is_lab1.model.Mood;
import ru.ifmo.se.is_lab1.model.WeaponType;
import ru.ifmo.se.is_lab1.repository.CarRepository;
import ru.ifmo.se.is_lab1.repository.CoordinatesRepository;
import ru.ifmo.se.is_lab1.repository.HumanBeingRepository;
import ru.ifmo.se.is_lab1.service.event.HumanBeingEvent;
import ru.ifmo.se.is_lab1.service.event.HumanBeingEventPublisher;
import ru.ifmo.se.is_lab1.service.event.HumanBeingEventType;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HumanBeingServiceTest {

    @Mock
    private HumanBeingRepository humanBeingRepository;
    @Mock
    private HumanBeingMapper humanBeingMapper;
    @Mock
    private CoordinatesRepository coordinatesRepository;
    @Mock
    private CarService carService;
    @Mock
    private CarRepository carRepository;
    @Mock
    private HumanBeingEventPublisher eventPublisher;

    @InjectMocks
    private HumanBeingService humanBeingService;

    private HumanBeingFormDto form;

    @BeforeEach
    void setUp() {
        form = createForm();
    }

    @Test
    void findAllShouldDelegateToRepositoryWithSpecification() {
        Pageable pageable = PageRequest.of(0, 20);
        HumanBeing human = mock(HumanBeing.class);
        HumanBeingDto dto = new HumanBeingDto();
        when(humanBeingRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(human)));
        when(humanBeingMapper.toDto(human)).thenReturn(dto);

        Page<HumanBeingDto> result = humanBeingService.findAll(new HumanBeingFilter(), pageable);

        assertThat(result.getContent()).containsExactly(dto);
    }

    @Test
    void findAllShouldReturnMappedList() {
        HumanBeing human = mock(HumanBeing.class);
        HumanBeingDto dto = new HumanBeingDto();
        when(humanBeingRepository.findAll()).thenReturn(List.of(human));
        when(humanBeingMapper.toDto(human)).thenReturn(dto);

        List<HumanBeingDto> result = humanBeingService.findAll();

        assertThat(result).containsExactly(dto);
    }

    @Test
    void findByIdShouldReturnDto() {
        HumanBeing human = mock(HumanBeing.class);
        HumanBeingDto dto = new HumanBeingDto();
        when(humanBeingRepository.findById(1L)).thenReturn(Optional.of(human));
        when(humanBeingMapper.toDto(human)).thenReturn(dto);

        HumanBeingDto result = humanBeingService.findById(1L);

        assertThat(result).isSameAs(dto);
    }

    @Test
    void findByIdShouldThrowWhenHumanMissing() {
        when(humanBeingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> humanBeingService.findById(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Человек с указанным идентификатором не найден");
    }

    @Test
    void createShouldPersistHumanAndPublishEvents() {
        form.setCarId(null);
        HumanBeingDto dto = new HumanBeingDto();
        when(coordinatesRepository.save(any(Coordinates.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(humanBeingRepository.saveAndFlush(any(HumanBeing.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(humanBeingMapper.toDto(any(HumanBeing.class))).thenReturn(dto);
        when(humanBeingRepository.count()).thenReturn(5L);
        when(humanBeingRepository.sumImpactSpeed()).thenReturn(20L);

        HumanBeingDto result = humanBeingService.create(form);

        assertThat(result).isSameAs(dto);

        ArgumentCaptor<HumanBeing> humanCaptor = ArgumentCaptor.forClass(HumanBeing.class);
        verify(humanBeingRepository).saveAndFlush(humanCaptor.capture());
        HumanBeing saved = humanCaptor.getValue();
        assertThat(saved.getName()).isEqualTo(form.getName());
        assertThat(saved.getImpactSpeed()).isEqualTo(form.getImpactSpeed());
        assertThat(saved.getHasToothpick()).isTrue();

        ArgumentCaptor<HumanBeingEvent> eventCaptor = ArgumentCaptor.forClass(HumanBeingEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        HumanBeingEvent event = eventCaptor.getValue();
        assertThat(event.getType()).isEqualTo(HumanBeingEventType.CREATED);
        assertThat(event.getHuman()).isSameAs(dto);

        ArgumentCaptor<HumanBeingSummary> summaryCaptor = ArgumentCaptor.forClass(HumanBeingSummary.class);
        verify(eventPublisher).publishSummary(summaryCaptor.capture());
        HumanBeingSummary summary = summaryCaptor.getValue();
        assertThat(summary.getTotalCount()).isEqualTo(5L);
        assertThat(summary.getTotalImpactSpeed()).isEqualTo(20L);
    }

    @Test
    void updateShouldReuseExistingCoordinates() {
        Coordinates coordinates = new Coordinates(1, 1f);
        HumanBeing human = new HumanBeing("Old", coordinates, false, false, 10, "Song", WeaponType.BAT, Mood.SADNESS, null);
        form.setCarId(2L);
        Car car = new Car("Car", true);
        when(humanBeingRepository.findById(1L)).thenReturn(Optional.of(human));
        when(carService.getEntity(2L)).thenReturn(car);
        when(humanBeingMapper.toDto(human)).thenReturn(new HumanBeingDto());
        when(humanBeingRepository.count()).thenReturn(0L);
        when(humanBeingRepository.sumImpactSpeed()).thenReturn(0L);

        humanBeingService.update(1L, form);

        assertThat(coordinates.getX()).isEqualTo(form.getCoordinatesX());
        assertThat(coordinates.getY()).isEqualTo(form.getCoordinatesY());
        verify(coordinatesRepository, never()).save(any());
        verify(humanBeingMapper).updateEntity(human, form, coordinates, car);
        verify(eventPublisher).publish(any(HumanBeingEvent.class));
        verify(eventPublisher).publishSummary(any(HumanBeingSummary.class));
    }

    @Test
    void updateShouldCreateCoordinatesWhenMissing() {
        HumanBeing human = new HumanBeing("Old", null, false, false, 10, "Song", WeaponType.BAT, Mood.SADNESS, null);
        Coordinates created = new Coordinates(1, 2f);
        when(humanBeingRepository.findById(1L)).thenReturn(Optional.of(human));
        when(coordinatesRepository.save(any(Coordinates.class))).thenReturn(created);
        when(humanBeingMapper.toDto(human)).thenReturn(new HumanBeingDto());
        when(humanBeingRepository.count()).thenReturn(0L);
        when(humanBeingRepository.sumImpactSpeed()).thenReturn(0L);

        humanBeingService.update(1L, form);

        verify(coordinatesRepository).save(any(Coordinates.class));
        verify(humanBeingMapper).updateEntity(human, form, created, null);
    }

    @Test
    void deleteShouldRemoveHumanAndPublishEvent() {
        HumanBeing human = mock(HumanBeing.class);
        HumanBeingDto dto = new HumanBeingDto();
        when(humanBeingRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(human));
        when(humanBeingMapper.toDto(human)).thenReturn(dto);
        when(humanBeingRepository.count()).thenReturn(1L);
        when(humanBeingRepository.sumImpactSpeed()).thenReturn(5L);

        humanBeingService.delete(1L);

        verify(humanBeingRepository).delete(human);
        verify(eventPublisher).publish(any(HumanBeingEvent.class));
        verify(eventPublisher).publishSummary(any(HumanBeingSummary.class));
    }

    @Test
    void sumImpactSpeedShouldReturnZeroWhenRepositoryReturnsNull() {
        when(humanBeingRepository.sumImpactSpeed()).thenReturn(null);

        assertThat(humanBeingService.sumImpactSpeed()).isZero();
    }

    @Test
    void getSummaryShouldReturnRepositoryValues() {
        when(humanBeingRepository.count()).thenReturn(3L);
        when(humanBeingRepository.sumImpactSpeed()).thenReturn(15L);

        HumanBeingSummary summary = humanBeingService.getSummary();

        assertThat(summary.getTotalCount()).isEqualTo(3L);
        assertThat(summary.getTotalImpactSpeed()).isEqualTo(15L);
    }

    @Test
    void countByImpactSpeedLessThanShouldDelegateToRepository() {
        when(humanBeingRepository.countByImpactSpeedLessThan(100)).thenReturn(4L);

        long result = humanBeingService.countByImpactSpeedLessThan(100);

        assertThat(result).isEqualTo(4L);
    }

    @Test
    void countByImpactSpeedLessThanShouldRejectNonPositiveThreshold() {
        assertThatThrownBy(() -> humanBeingService.countByImpactSpeedLessThan(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Порог скорости удара должен быть положительным");
    }

    @Test
    void findBySoundtrackPrefixShouldMapResults() {
        HumanBeing human = mock(HumanBeing.class);
        HumanBeingDto dto = new HumanBeingDto();
        doReturn(List.of(human)).when(humanBeingRepository)
                .findBySoundtrackNameStartingWithIgnoreCase("pre");
        when(humanBeingMapper.toDto(human)).thenReturn(dto);

        List<HumanBeingDto> result = humanBeingService.findBySoundtrackPrefix("pre");

        assertThat(result).containsExactly(dto);
    }

    @Test
    void bulkUpdateMoodShouldPublishWhenUpdated() {
        when(humanBeingRepository.bulkUpdateMood(Mood.SADNESS, Mood.GLOOM)).thenReturn(2);
        when(humanBeingRepository.count()).thenReturn(1L);
        when(humanBeingRepository.sumImpactSpeed()).thenReturn(2L);

        int updated = humanBeingService.bulkUpdateMood(Mood.SADNESS, Mood.GLOOM);

        assertThat(updated).isEqualTo(2);
        ArgumentCaptor<HumanBeingEvent> eventCaptor = ArgumentCaptor.forClass(HumanBeingEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        HumanBeingEvent event = eventCaptor.getValue();
        assertThat(event.getType()).isEqualTo(HumanBeingEventType.MOOD_UPDATED);
        assertThat(event.getHuman()).isNull();
        verify(eventPublisher).publishSummary(any(HumanBeingSummary.class));
    }

    @Test
    void bulkUpdateMoodShouldNotPublishWhenNoRowsUpdated() {
        when(humanBeingRepository.bulkUpdateMood(Mood.SADNESS, Mood.GLOOM)).thenReturn(0);

        int updated = humanBeingService.bulkUpdateMood(Mood.SADNESS, Mood.GLOOM);

        assertThat(updated).isZero();
        verify(eventPublisher, never()).publish(any(HumanBeingEvent.class));
    }

    @Test
    void updateMoodToGloomShouldPublishWhenUpdated() {
        when(humanBeingRepository.updateMoodForAll(Mood.GLOOM)).thenReturn(3);
        when(humanBeingRepository.count()).thenReturn(1L);
        when(humanBeingRepository.sumImpactSpeed()).thenReturn(2L);

        int updated = humanBeingService.updateMoodToGloom();

        assertThat(updated).isEqualTo(3);
        ArgumentCaptor<HumanBeingEvent> gloomCaptor = ArgumentCaptor.forClass(HumanBeingEvent.class);
        verify(eventPublisher).publish(gloomCaptor.capture());
        HumanBeingEvent gloomEvent = gloomCaptor.getValue();
        assertThat(gloomEvent.getType()).isEqualTo(HumanBeingEventType.MOOD_UPDATED);
        verify(eventPublisher).publishSummary(any(HumanBeingSummary.class));
    }

    @Test
    void updateMoodToGloomShouldNotPublishWhenZero() {
        when(humanBeingRepository.updateMoodForAll(Mood.GLOOM)).thenReturn(0);

        int updated = humanBeingService.updateMoodToGloom();

        assertThat(updated).isZero();
        verify(eventPublisher, never()).publish(any(HumanBeingEvent.class));
    }

    @Test
    void assignDefaultCarShouldReturnZeroWhenEveryoneHasCar() {
        when(humanBeingRepository.findByCarIsNullForUpdate()).thenReturn(List.of());

        int result = humanBeingService.assignDefaultCarToHeroesWithoutCar();

        assertThat(result).isZero();
        verify(carRepository, never()).save(any());
    }

    @Test
    void assignDefaultCarShouldSaveCarAndPublishWhenUpdated() {
        HumanBeing hero = mock(HumanBeing.class);
        when(humanBeingRepository.findByCarIsNullForUpdate()).thenReturn(List.of(hero));
        Car persisted = new Car("Default", true);
        when(carRepository.save(any(Car.class))).thenReturn(persisted);
        when(humanBeingRepository.assignCarToAllWithoutCar(persisted)).thenReturn(5);
        when(humanBeingRepository.count()).thenReturn(1L);
        when(humanBeingRepository.sumImpactSpeed()).thenReturn(1L);

        int updated = humanBeingService.assignDefaultCarToHeroesWithoutCar();

        assertThat(updated).isEqualTo(5);
        verify(carRepository).save(any(Car.class));
        verify(humanBeingRepository).assignCarToAllWithoutCar(persisted);
        verify(eventPublisher).publish(any(HumanBeingEvent.class));
        verify(eventPublisher).publishSummary(any(HumanBeingSummary.class));
    }

    @Test
    void assignCarShouldFetchEntitiesAndPublishEvent() {
        HumanBeing human = new HumanBeing("Hero", new Coordinates(1, 1f), true, true, 10, "Song", WeaponType.BAT, Mood.SADNESS, null);
        Car car = new Car("Car", true);
        HumanBeingDto dto = new HumanBeingDto();
        when(humanBeingRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(human));
        when(carService.getEntity(2L)).thenReturn(car);
        when(humanBeingMapper.toDto(human)).thenReturn(dto);
        when(humanBeingRepository.count()).thenReturn(1L);
        when(humanBeingRepository.sumImpactSpeed()).thenReturn(1L);

        HumanBeingDto result = humanBeingService.assignCar(1L, 2L);

        assertThat(result).isSameAs(dto);
        assertThat(human.getCar()).isSameAs(car);
        verify(eventPublisher).publish(any(HumanBeingEvent.class));
        verify(eventPublisher).publishSummary(any(HumanBeingSummary.class));
    }

    private HumanBeingFormDto createForm() {
        HumanBeingFormDto form = new HumanBeingFormDto();
        form.setName("Ivan");
        form.setCoordinatesX(10);
        form.setCoordinatesY(5.5f);
        form.setRealHero(true);
        form.setHasToothpick(true);
        form.setImpactSpeed(100);
        form.setSoundtrackName("Sound");
        form.setWeaponType(WeaponType.SHOTGUN);
        form.setMood(Mood.SADNESS);
        return form;
    }
}
