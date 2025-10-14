package ru.ifmo.se.is_lab1.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.se.is_lab1.domain.Car;
import ru.ifmo.se.is_lab1.domain.Mood;
import ru.ifmo.se.is_lab1.domain.MusicBand;
import ru.ifmo.se.is_lab1.repository.CarRepository;
import ru.ifmo.se.is_lab1.repository.MusicBandRepository;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MusicBandRestControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MusicBandRepository musicBandRepository;

    @Autowired
    private CarRepository carRepository;

    @BeforeEach
    void setUp() {
        musicBandRepository.deleteAll();
        carRepository.deleteAll();
    }

    @Test
    void countByImpactSpeedLessThanReturnsExpectedValue() throws Exception {
        createBand("Alpha", new BigDecimal("10"), Mood.SADNESS, null);
        createBand("Beta", new BigDecimal("30"), Mood.LONGING, null);
        createBand("Gamma", new BigDecimal("120"), Mood.GLOOM, null);

        mockMvc.perform(get("/api/bands/impact-speed/count").param("threshold", "50"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("2"));
    }

    @Test
    void countByImpactSpeedLessThanRejectsNonPositiveThreshold() throws Exception {
        mockMvc.perform(get("/api/bands/impact-speed/count").param("threshold", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateMoodToGloomChangesAllEntities() throws Exception {
        createBand("Joy", new BigDecimal("15"), Mood.SADNESS, null);
        createBand("Calm", new BigDecimal("25"), Mood.LONGING, null);
        createBand("Rage", new BigDecimal("35"), Mood.SADNESS, null);

        mockMvc.perform(post("/api/bands/mood/gloom"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("3"));

        assertThat(musicBandRepository.findAll())
                .hasSize(3)
                .allSatisfy(band -> assertThat(band.getMood()).isEqualTo(Mood.GLOOM));
    }

    @Test
    void assignDefaultCarUpdatesOnlyBandsWithoutCars() throws Exception {
        Car existingCar = new Car();
        existingCar.setName("Mustang");
        existingCar.setModel("GT");
        existingCar.setColor("Синий");
        Car persistedExistingCar = carRepository.save(existingCar);

        createBand("WithCar", new BigDecimal("60"), Mood.SADNESS, persistedExistingCar);
        createBand("WithoutOne", new BigDecimal("40"), Mood.LONGING, null);
        createBand("WithoutTwo", new BigDecimal("80"), Mood.GLOOM, null);

        mockMvc.perform(post("/api/bands/cars/assign-default"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("2"));

        assertThat(carRepository.findAll())
                .hasSize(2)
                .anySatisfy(car -> {
                    if ("Lada Kalina".equals(car.getName())) {
                        assertThat(car.getModel()).isEqualTo("Lada Kalina");
                        assertThat(car.getColor()).isEqualTo("Красный");
                    }
                });

        assertThat(musicBandRepository.findAll())
                .hasSize(3)
                .anySatisfy(band -> {
                    if ("WithCar".equals(band.getName())) {
                        assertThat(band.getCar()).isNotNull();
                        assertThat(band.getCar().getId()).isEqualTo(persistedExistingCar.getId());
                    }
                })
                .filteredOn(band -> band.getName().startsWith("Without"))
                .allSatisfy(band -> {
                    assertThat(band.getCar()).isNotNull();
                    assertThat(band.getCar().getName()).isEqualTo("Lada Kalina");
                    assertThat(band.getCar().getColor()).isEqualTo("Красный");
                });
    }

    private void createBand(String name, BigDecimal impactSpeed, Mood mood, Car car) {
        MusicBand band = new MusicBand();
        band.setName(name);
        band.setImpactSpeed(impactSpeed);
        band.setSoundtrackName(name + " track");
        band.setMood(mood);
        band.setCar(car);
        musicBandRepository.save(band);
    }
}
