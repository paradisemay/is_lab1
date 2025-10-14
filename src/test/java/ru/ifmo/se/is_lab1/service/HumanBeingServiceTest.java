package ru.ifmo.se.is_lab1.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.se.is_lab1.dto.HumanBeingDto;
import ru.ifmo.se.is_lab1.dto.HumanBeingFilter;
import ru.ifmo.se.is_lab1.dto.HumanBeingFormDto;
import ru.ifmo.se.is_lab1.model.Car;
import ru.ifmo.se.is_lab1.model.Mood;
import ru.ifmo.se.is_lab1.model.WeaponType;
import ru.ifmo.se.is_lab1.repository.LegacyCarRepository;
import ru.ifmo.se.is_lab1.service.event.HumanBeingEventPublisher;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class HumanBeingServiceTest {

    @Autowired
    private HumanBeingService humanBeingService;

    @Autowired
    private LegacyCarRepository carRepository;

    @MockBean
    private HumanBeingEventPublisher eventPublisher;

    @Test
    void createHumanAndCalculateSum() {
        Car car = carRepository.save(new Car("Volga", true));

        HumanBeingFormDto form = new HumanBeingFormDto();
        form.setName("Aurora");
        form.setCoordinateX(12.0);
        form.setCoordinateY(5.0);
        form.setRealHero(true);
        form.setHasToothpick(Boolean.TRUE);
        form.setImpactSpeed(15.5);
        form.setSoundtrackName("Northern Lights");
        form.setMinutesOfWaiting(30L);
        form.setWeaponType(WeaponType.RIFLE);
        form.setMood(Mood.CALM);
        form.setCarId(car.getId());

        HumanBeingDto created = humanBeingService.create(form);

        assertThat(created.getId()).isNotNull();
        assertThat(humanBeingService.sumImpactSpeed()).isEqualTo(15.5);
        assertThat(created.getCar()).isNotNull();
        assertThat(created.getCar().getName()).isEqualTo("Volga");
        assertThat(created.getCoordinates()).isNotNull();
    }

    @Test
    void filterAndBulkUpdateMood() {
        HumanBeingFormDto first = new HumanBeingFormDto();
        first.setName("Thunder");
        first.setCoordinateX(1.0);
        first.setCoordinateY(2.0);
        first.setRealHero(false);
        first.setHasToothpick(Boolean.FALSE);
        first.setImpactSpeed(9.0);
        first.setSoundtrackName("Storm Rising");
        first.setMinutesOfWaiting(45L);
        first.setWeaponType(WeaponType.AXE);
        first.setMood(Mood.SORROW);
        humanBeingService.create(first);

        HumanBeingFormDto second = new HumanBeingFormDto();
        second.setName("Sunny");
        second.setCoordinateX(3.0);
        second.setCoordinateY(4.0);
        second.setRealHero(true);
        second.setHasToothpick(Boolean.TRUE);
        second.setImpactSpeed(5.0);
        second.setSoundtrackName("Sunrise Melody");
        second.setMinutesOfWaiting(20L);
        second.setWeaponType(WeaponType.SHOTGUN);
        second.setMood(Mood.GLOOM);
        humanBeingService.create(second);

        HumanBeingFilter filter = new HumanBeingFilter();
        filter.setName("Sun");

        List<HumanBeingDto> filtered = humanBeingService.findAll(filter, PageRequest.of(0, 10))
                .getContent();
        assertThat(filtered).hasSize(1);
        assertThat(filtered.getFirst().getName()).isEqualTo("Sunny");

        int updated = humanBeingService.bulkUpdateMood(Mood.SORROW, Mood.RAGE);
        assertThat(updated).isEqualTo(1);

        List<HumanBeingDto> byPrefix = humanBeingService.findBySoundtrackPrefix("Sto");
        assertThat(byPrefix).hasSize(1);
        assertThat(byPrefix.getFirst().getMood()).isEqualTo(Mood.RAGE);
    }
}
