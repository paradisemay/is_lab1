package ru.ifmo.se.is_lab1.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import ru.ifmo.se.is_lab1.dto.CarDto;
import ru.ifmo.se.is_lab1.dto.HumanBeingDto;
import ru.ifmo.se.is_lab1.dto.HumanBeingFilter;
import ru.ifmo.se.is_lab1.dto.HumanBeingFormDto;
import ru.ifmo.se.is_lab1.model.Mood;
import ru.ifmo.se.is_lab1.model.WeaponType;
import ru.ifmo.se.is_lab1.repository.CarRepository;
import ru.ifmo.se.is_lab1.repository.CoordinatesRepository;
import ru.ifmo.se.is_lab1.repository.HumanBeingRepository;
import ru.ifmo.se.is_lab1.service.event.HumanBeingEventPublisher;

@SpringBootTest
@Transactional
@ContextConfiguration(initializers = HumanBeingServiceIntegrationTest.DataSourceInitializer.class)
class HumanBeingServiceIntegrationTest {

    @Autowired
    private HumanBeingService humanBeingService;

    @Autowired
    private CarService carService;

    @Autowired
    private HumanBeingRepository humanBeingRepository;

    @Autowired
    private CoordinatesRepository coordinatesRepository;

    @Autowired
    private CarRepository carRepository;

    @MockBean
    private HumanBeingEventPublisher eventPublisher;

    @BeforeEach
    void clearDatabase() {
        humanBeingRepository.deleteAll();
        coordinatesRepository.deleteAll();
        carRepository.deleteAll();
    }

    @Test
    void createHumanBeingAndFilterByName() {
        CarDto car = carService.create("Test car", Boolean.TRUE);

        HumanBeingFormDto form = new HumanBeingFormDto();
        form.setName("Ivan");
        form.setCoordinatesX(12);
        form.setCoordinatesY(4.5f);
        form.setRealHero(Boolean.TRUE);
        form.setHasToothpick(Boolean.TRUE);
        form.setImpactSpeed(123);
        form.setSoundtrackName("Hero theme");
        form.setWeaponType(WeaponType.SHOTGUN);
        form.setMood(Mood.GLOOM);
        form.setCarId(car.getId());

        HumanBeingDto created = humanBeingService.create(form);
        assertThat(created.getId()).isNotNull();
        assertThat(created.getCar()).isNotNull();
        assertThat(created.getCar().getId()).isEqualTo(car.getId());
        assertThat(created.getCoordinates()).isNotNull();
        assertThat(created.getCoordinates().getX()).isEqualTo(12);

        HumanBeingFilter filter = new HumanBeingFilter();
        filter.setName("Ivan");

        Page<HumanBeingDto> result = humanBeingService.findAll(filter, PageRequest.of(0, 10));
        assertThat(result.getContent()).extracting(HumanBeingDto::getName).containsExactly("Ivan");
    }

    static class DataSourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                    "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
                    "spring.jpa.hibernate.ddl-auto=update",
                    "spring.flyway.enabled=false");
        }
    }
}
