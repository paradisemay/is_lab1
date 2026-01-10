package ru.ifmo.se.is_lab1.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import ru.ifmo.se.is_lab1.config.TestConfig;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import ru.ifmo.se.is_lab1.domain.Car;
import ru.ifmo.se.is_lab1.domain.Coordinates;
import ru.ifmo.se.is_lab1.domain.HumanBeing;
import ru.ifmo.se.is_lab1.dto.CarDto;
import ru.ifmo.se.is_lab1.model.Mood;
import ru.ifmo.se.is_lab1.model.WeaponType;
import ru.ifmo.se.is_lab1.repository.CarRepository;
import ru.ifmo.se.is_lab1.repository.CoordinatesRepository;
import ru.ifmo.se.is_lab1.repository.HumanBeingRepository;
import ru.ifmo.se.is_lab1.service.CarService;
import ru.ifmo.se.is_lab1.service.HumanBeingService;

@SpringBootTest
@ContextConfiguration(initializers = HumanBeingConcurrentAssignmentIntegrationTest.DataSourceInitializer.class)
@Import(TestConfig.class)
class HumanBeingConcurrentAssignmentIntegrationTest {

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

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);
        humanBeingRepository.deleteAll();
        carRepository.deleteAll();
        coordinatesRepository.deleteAll();
    }

    @Test
    void concurrentDefaultAssignmentsCreateSingleDefaultCar() throws Exception {
        HumanBeing human = createHumanWithoutCar("Parallel hero");

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        Future<Integer> first = executor.submit(() -> {
            startLatch.await();
            return humanBeingService.assignDefaultCarToHeroesWithoutCar();
        });
        Future<Integer> second = executor.submit(() -> {
            startLatch.await();
            return humanBeingService.assignDefaultCarToHeroesWithoutCar();
        });

        startLatch.countDown();

        int totalUpdated = first.get() + second.get();
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        HumanBeing reloaded = humanBeingRepository.findById(human.getId()).orElseThrow();
        long defaultCars = carRepository.findAll().stream()
                .filter(car -> "Lada Kalina".equals(car.getName()))
                .count();

        assertThat(totalUpdated).isEqualTo(1);
        assertThat(reloaded.getCar()).isNotNull();
        assertThat(defaultCars).isEqualTo(1);
    }

    @Test
    void explicitAssignmentBlocksDefaultFallback() throws Exception {
        HumanBeing human = createHumanWithoutCar("Chosen one");
        CarDto manualCar = carService.create("Matrix", true);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch carAssigned = new CountDownLatch(1);
        CountDownLatch releaseTransaction = new CountDownLatch(1);

        Future<Void> assignCarFuture = executor.submit(() -> {
            transactionTemplate.executeWithoutResult(status -> {
                humanBeingService.assignCar(human.getId(), manualCar.getId());
                carAssigned.countDown();
                awaitLatch(releaseTransaction);
            });
            return null;
        });

        Future<Integer> defaultAssignmentFuture = executor.submit(() -> {
            carAssigned.await();
            return humanBeingService.assignDefaultCarToHeroesWithoutCar();
        });

        carAssigned.await();
        releaseTransaction.countDown();

        assignCarFuture.get();
        int defaultAssignments = defaultAssignmentFuture.get();

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        HumanBeing reloaded = humanBeingRepository.findById(human.getId()).orElseThrow();
        long defaultCars = carRepository.findAll().stream()
                .filter(car -> "Lada Kalina".equals(car.getName()))
                .count();

        assertThat(defaultAssignments).isZero();
        assertThat(reloaded.getCar().getId()).isEqualTo(manualCar.getId());
        assertThat(defaultCars).isZero();
    }

    private HumanBeing createHumanWithoutCar(String name) {
        Coordinates coordinates = coordinatesRepository.save(new Coordinates(10, 1.0f));
        HumanBeing humanBeing = new HumanBeing(
                name,
                coordinates,
                Boolean.TRUE,
                true,
                100,
                "Song",
                WeaponType.BAT,
                Mood.SADNESS,
                null
        );
        return humanBeingRepository.save(humanBeing);
    }

    private void awaitLatch(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for latch", e);
        }
    }

    static class DataSourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                    "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
                    "spring.jpa.hibernate.ddl-auto=update",
                    "spring.flyway.enabled=false",
                    "app.minio.enabled=false");
        }
    }
}
