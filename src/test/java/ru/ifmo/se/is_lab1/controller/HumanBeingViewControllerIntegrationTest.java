package ru.ifmo.se.is_lab1.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import ru.ifmo.se.is_lab1.dto.HumanBeingFormDto;
import ru.ifmo.se.is_lab1.model.Mood;
import ru.ifmo.se.is_lab1.model.WeaponType;
import ru.ifmo.se.is_lab1.repository.CarRepository;
import ru.ifmo.se.is_lab1.repository.CoordinatesRepository;
import ru.ifmo.se.is_lab1.repository.HumanBeingRepository;
import ru.ifmo.se.is_lab1.service.CarService;
import ru.ifmo.se.is_lab1.service.HumanBeingService;
import ru.ifmo.se.is_lab1.service.event.HumanBeingEventPublisher;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ContextConfiguration(initializers = HumanBeingViewControllerIntegrationTest.DataSourceInitializer.class)
class HumanBeingViewControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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
    void setUp() {
        humanBeingRepository.deleteAll();
        coordinatesRepository.deleteAll();
        carRepository.deleteAll();

        var car = carService.create("List car", Boolean.TRUE);
        HumanBeingFormDto form = new HumanBeingFormDto();
        form.setName("List human");
        form.setCoordinatesX(10);
        form.setCoordinatesY(7.0f);
        form.setRealHero(Boolean.FALSE);
        form.setHasToothpick(Boolean.TRUE);
        form.setImpactSpeed(150);
        form.setSoundtrackName("List soundtrack");
        form.setMood(Mood.SADNESS);
        form.setWeaponType(WeaponType.KNIFE);
        form.setCarId(car.getId());
        humanBeingService.create(form);
    }

    @Test
    void listPageRendersWithModel() throws Exception {
        mockMvc.perform(get("/humans"))
                .andExpect(status().isOk())
                .andExpect(view().name("humans/list"))
                .andExpect(model().attributeExists("humans", "moods", "weaponTypes", "cars"));
    }

    @Test
    void createValidationErrorReturnsForm() throws Exception {
        mockMvc.perform(post("/humans")
                        .param("name", "")
                        .param("coordinatesX", "5")
                        .param("coordinatesY", "1.5")
                        .param("hasToothpick", "true")
                        .param("impactSpeed", "100")
                        .param("soundtrackName", "Track"))
                .andExpect(status().isOk())
                .andExpect(view().name("humans/create"))
                .andExpect(model().attributeHasFieldErrors("human", "name"));
    }

    static class DataSourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                    "spring.datasource.url=jdbc:h2:mem:controllerdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
                    "spring.jpa.hibernate.ddl-auto=update",
                    "spring.flyway.enabled=false");
        }
    }
}
