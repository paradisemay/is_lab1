package ru.ifmo.se.is_lab1.controller;

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
import org.springframework.data.domain.Sort;
import ru.ifmo.se.is_lab1.dto.AssignCarRequest;
import ru.ifmo.se.is_lab1.dto.HumanBeingDto;
import ru.ifmo.se.is_lab1.dto.HumanBeingFilter;
import ru.ifmo.se.is_lab1.dto.HumanBeingFormDto;
import ru.ifmo.se.is_lab1.dto.MoodChangeRequest;
import ru.ifmo.se.is_lab1.model.Mood;
import ru.ifmo.se.is_lab1.model.WeaponType;
import ru.ifmo.se.is_lab1.service.HumanBeingService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HumanBeingRestControllerTest {

    @Mock
    private HumanBeingService humanBeingService;

    @InjectMocks
    private HumanBeingRestController controller;

    @Test
    void findAllShouldCreateFilterWithAllParameters() {
        Page<HumanBeingDto> page = new PageImpl<>(List.of(), PageRequest.of(1, 5), 0);
        when(humanBeingService.findAll(any(HumanBeingFilter.class), any(Pageable.class))).thenReturn(page);

        Page<HumanBeingDto> result = controller.findAll(
                Optional.of(1),
                Optional.of(5),
                Optional.of("name"),
                Optional.of("DESC"),
                Optional.of("Ivan"),
                Optional.of(Mood.SADNESS),
                Optional.of(WeaponType.SHOTGUN),
                Optional.of(10),
                Optional.of(20),
                Optional.of("Song"),
                Optional.of(3L),
                Optional.of(true),
                Optional.of(false)
        );

        assertThat(result).isSameAs(page);

        ArgumentCaptor<HumanBeingFilter> filterCaptor = ArgumentCaptor.forClass(HumanBeingFilter.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(humanBeingService).findAll(filterCaptor.capture(), pageableCaptor.capture());

        HumanBeingFilter filter = filterCaptor.getValue();
        assertThat(filter.getName()).isEqualTo("Ivan");
        assertThat(filter.getMood()).isEqualTo(Mood.SADNESS);
        assertThat(filter.getWeaponType()).isEqualTo(WeaponType.SHOTGUN);
        assertThat(filter.getMinImpactSpeed()).isEqualTo(10);
        assertThat(filter.getMaxImpactSpeed()).isEqualTo(20);
        assertThat(filter.getSoundtrackPrefix()).isEqualTo("Song");
        assertThat(filter.getCarId()).isEqualTo(3L);
        assertThat(filter.getRealHero()).isTrue();
        assertThat(filter.getHasToothpick()).isFalse();

        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getPageSize()).isEqualTo(5);
        Sort.Order order = pageable.getSort().getOrderFor("name");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void findAllShouldUseDefaultsWhenParametersMissing() {
        Page<HumanBeingDto> page = Page.empty();
        when(humanBeingService.findAll(any(HumanBeingFilter.class), any(Pageable.class))).thenReturn(page);

        Page<HumanBeingDto> result = controller.findAll(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );

        assertThat(result).isSameAs(page);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(humanBeingService).findAll(any(HumanBeingFilter.class), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(20);
        Sort.Order order = pageable.getSort().getOrderFor("id");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void findByIdShouldDelegateToService() {
        HumanBeingDto dto = new HumanBeingDto();
        when(humanBeingService.findById(1L)).thenReturn(dto);

        HumanBeingDto result = controller.findById(1L);

        assertThat(result).isSameAs(dto);
    }

    @Test
    void createShouldPassFormToService() {
        HumanBeingFormDto form = new HumanBeingFormDto();
        HumanBeingDto dto = new HumanBeingDto();
        when(humanBeingService.create(form)).thenReturn(dto);

        HumanBeingDto result = controller.create(form);

        assertThat(result).isSameAs(dto);
    }

    @Test
    void updateShouldPassParametersToService() {
        HumanBeingFormDto form = new HumanBeingFormDto();
        HumanBeingDto dto = new HumanBeingDto();
        when(humanBeingService.update(1L, form)).thenReturn(dto);

        HumanBeingDto result = controller.update(1L, form);

        assertThat(result).isSameAs(dto);
    }

    @Test
    void deleteShouldDelegateToService() {
        controller.delete(5L);

        verify(humanBeingService).delete(5L);
    }

    @Test
    void sumImpactSpeedShouldReturnServiceResult() {
        when(humanBeingService.sumImpactSpeed()).thenReturn(42L);

        long result = controller.sumImpactSpeed();

        assertThat(result).isEqualTo(42L);
    }

    @Test
    void countByImpactSpeedLessThanShouldDelegate() {
        when(humanBeingService.countByImpactSpeedLessThan(10)).thenReturn(3L);

        long result = controller.countByImpactSpeedLessThan(10);

        assertThat(result).isEqualTo(3L);
    }

    @Test
    void findBySoundtrackPrefixShouldDelegate() {
        HumanBeingDto dto = new HumanBeingDto();
        when(humanBeingService.findBySoundtrackPrefix("pre")).thenReturn(List.of(dto));

        List<HumanBeingDto> result = controller.findBySoundtrackPrefix("pre");

        assertThat(result).containsExactly(dto);
    }

    @Test
    void bulkUpdateMoodShouldDelegate() {
        MoodChangeRequest request = new MoodChangeRequest();
        request.setSourceMood(Mood.SADNESS);
        request.setTargetMood(Mood.GLOOM);
        when(humanBeingService.bulkUpdateMood(Mood.SADNESS, Mood.GLOOM)).thenReturn(7);

        int updated = controller.bulkUpdateMood(request);

        assertThat(updated).isEqualTo(7);
    }

    @Test
    void assignCarShouldDelegate() {
        AssignCarRequest request = new AssignCarRequest();
        request.setCarId(9L);
        HumanBeingDto dto = new HumanBeingDto();
        when(humanBeingService.assignCar(1L, 9L)).thenReturn(dto);

        HumanBeingDto result = controller.assignCar(1L, request);

        assertThat(result).isSameAs(dto);
    }

    @Test
    void updateMoodToGloomShouldDelegate() {
        when(humanBeingService.updateMoodToGloom()).thenReturn(4);

        int updated = controller.updateMoodToGloom();

        assertThat(updated).isEqualTo(4);
    }

    @Test
    void assignDefaultCarShouldDelegate() {
        when(humanBeingService.assignDefaultCarToHeroesWithoutCar()).thenReturn(11);

        int updated = controller.assignDefaultCar();

        assertThat(updated).isEqualTo(11);
    }
}
