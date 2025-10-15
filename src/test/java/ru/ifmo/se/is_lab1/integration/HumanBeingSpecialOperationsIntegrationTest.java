package ru.ifmo.se.is_lab1.integration;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ru.ifmo.se.is_lab1.dto.HumanBeingDto;
import ru.ifmo.se.is_lab1.dto.MoodChangeRequest;
import ru.ifmo.se.is_lab1.model.Car;
import ru.ifmo.se.is_lab1.model.Coordinates;
import ru.ifmo.se.is_lab1.model.HumanBeing;
import ru.ifmo.se.is_lab1.model.Mood;
import ru.ifmo.se.is_lab1.model.WeaponType;
import ru.ifmo.se.is_lab1.repository.CarRepository;
import ru.ifmo.se.is_lab1.repository.CoordinatesRepository;
import ru.ifmo.se.is_lab1.repository.HumanBeingRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HumanBeingSpecialOperationsIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private HumanBeingRepository humanBeingRepository;

    @Autowired
    private CoordinatesRepository coordinatesRepository;

    @Autowired
    private CarRepository carRepository;

    @BeforeEach
    void setUp() {
        humanBeingRepository.deleteAll();
        carRepository.deleteAll();
        coordinatesRepository.deleteAll();
    }

    @Test
    void bulkUpdateMoodChangesRecords() {
        createHuman("Alice", Mood.SADNESS, 120, "Sunrise", null);
        createHuman("Bob", Mood.SADNESS, 150, "Sunset", null);
        createHuman("Charlie", Mood.GLOOM, 180, "Midnight", null);

        MoodChangeRequest request = new MoodChangeRequest();
        request.setSourceMood(Mood.SADNESS);
        request.setTargetMood(Mood.GLOOM);

        ResponseEntity<Integer> response = restTemplate.postForEntity("/api/humans/mood/bulk", request, Integer.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody()).isEqualTo(2);

        List<HumanBeing> humans = humanBeingRepository.findAll();
        long gloomyCount = humans.stream().filter(h -> h.getMood() == Mood.GLOOM).count();
        long sadnessCount = humans.stream().filter(h -> h.getMood() == Mood.SADNESS).count();

        Assertions.assertThat(gloomyCount).isEqualTo(3);
        Assertions.assertThat(sadnessCount).isZero();
    }

    @Test
    void countAndSearchOperationsWork() {
        createHuman("Alice", Mood.SADNESS, 120, "Sunrise", null);
        createHuman("Bob", Mood.LONGING, 180, "Sunshine", null);
        createHuman("Charlie", Mood.GLOOM, 400, "Moonlight", null);

        ResponseEntity<Long> countResponse = restTemplate.getForEntity("/api/humans/impact-speed/count?threshold=300", Long.class);
        Assertions.assertThat(countResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(countResponse.getBody()).isEqualTo(2L);

        ResponseEntity<HumanBeingDto[]> searchResponse = restTemplate.getForEntity("/api/humans/soundtrack?prefix=Sun", HumanBeingDto[].class);
        Assertions.assertThat(searchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(searchResponse.getBody()).hasSize(2);
    }

    @Test
    void assignDefaultCarAssignsVehicles() {
        HumanBeing withoutCar = createHuman("Daisy", Mood.SADNESS, 140, "Harmony", null);
        Car car = new Car("Retro", true);
        Car savedCar = carRepository.save(car);
        createHuman("Edward", Mood.LONGING, 160, "Rhythm", savedCar);

        ResponseEntity<Integer> response = restTemplate.postForEntity("/api/humans/cars/assign-default", null, Integer.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody()).isEqualTo(1);

        HumanBeing reloaded = humanBeingRepository.findById(withoutCar.getId()).orElseThrow();
        Assertions.assertThat(reloaded.getCar()).isNotNull();
        Assertions.assertThat(carRepository.findAll().stream().map(Car::getName)).contains("Lada Kalina");
    }

    private HumanBeing createHuman(String name, Mood mood, int impactSpeed, String soundtrack, Car car) {
        Coordinates coordinates = coordinatesRepository.save(new Coordinates(impactSpeed, 1.0f));
        HumanBeing humanBeing = new HumanBeing(
                name,
                coordinates,
                Boolean.TRUE,
                true,
                impactSpeed,
                soundtrack,
                WeaponType.KNIFE,
                mood,
                car
        );
        return humanBeingRepository.save(humanBeing);
    }
}
