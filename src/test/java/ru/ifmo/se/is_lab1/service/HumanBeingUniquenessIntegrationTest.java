package ru.ifmo.se.is_lab1.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;

import ru.ifmo.se.is_lab1.domain.ImportOperation;
import ru.ifmo.se.is_lab1.dto.HumanBeingFormDto;
import ru.ifmo.se.is_lab1.model.Mood;
import ru.ifmo.se.is_lab1.model.WeaponType;
import ru.ifmo.se.is_lab1.repository.CarRepository;
import ru.ifmo.se.is_lab1.repository.CoordinatesRepository;
import ru.ifmo.se.is_lab1.repository.HumanBeingRepository;
import ru.ifmo.se.is_lab1.repository.HumanBeingSpecifications;
import ru.ifmo.se.is_lab1.repository.ImportOperationRepository;
import ru.ifmo.se.is_lab1.service.event.HumanBeingEventPublisher;
import ru.ifmo.se.is_lab1.service.exception.HumanBeingUniquenessException;
import ru.ifmo.se.is_lab1.service.security.UserContextHolder;

@SpringBootTest
@ContextConfiguration(initializers = HumanBeingUniquenessIntegrationTest.DataSourceInitializer.class)
class HumanBeingUniquenessIntegrationTest {

    @Autowired
    private HumanBeingService humanBeingService;

    @Autowired
    private HumanImportService humanImportService;

    @Autowired
    private HumanBeingRepository humanBeingRepository;

    @Autowired
    private CoordinatesRepository coordinatesRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private ImportOperationRepository importOperationRepository;

    @Autowired
    private UserContextHolder userContextHolder;

    @MockBean
    private HumanBeingEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        importOperationRepository.deleteAll();
        humanBeingRepository.deleteAll();
        coordinatesRepository.deleteAll();
        carRepository.deleteAll();
        userContextHolder.setCurrentUser("uniqueness-tester", false);
    }

    @AfterEach
    void tearDown() {
        userContextHolder.clear();
    }

    @Test
    void concurrentCreationShouldRespectUniqueConstraints() throws Exception {
        CountDownLatch startLatch = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Callable<Throwable> task = () -> createHumanBeingConcurrently(startLatch);
        List<Future<Throwable>> results = List.of(
                executorService.submit(task),
                executorService.submit(task)
        );

        startLatch.countDown();

        long success = 0;
        long uniquenessFailures = 0;
        for (Future<Throwable> future : results) {
            Throwable result = future.get();
            if (result == null) {
                success++;
            } else if (result instanceof HumanBeingUniquenessException) {
                uniquenessFailures++;
            } else {
                throw new AssertionError("Unexpected exception", result);
            }
        }
        executorService.shutdown();

        assertThat(success).isEqualTo(1);
        assertThat(uniquenessFailures).isEqualTo(1);
        assertThat(humanBeingRepository.count()).isEqualTo(1);
    }

    @Test
    void concurrentImportShouldSurfaceUniqueConflicts() throws Exception {
        CountDownLatch startLatch = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Callable<Throwable> task = () -> importHumansConcurrently(startLatch);
        List<Future<Throwable>> results = List.of(
                executorService.submit(task),
                executorService.submit(task)
        );

        startLatch.countDown();

        long success = 0;
        long uniquenessFailures = 0;
        for (Future<Throwable> future : results) {
            Throwable result = future.get();
            if (result == null) {
                success++;
            } else if (result instanceof HumanBeingUniquenessException) {
                uniquenessFailures++;
            } else {
                throw new AssertionError("Unexpected exception", result);
            }
        }
        executorService.shutdown();

        assertThat(success).isEqualTo(1);
        assertThat(uniquenessFailures).isEqualTo(1);
        assertThat(humanBeingRepository.count()).isEqualTo(1);
        assertThat(importOperationRepository.count()).isEqualTo(2);
        assertThat(importOperationRepository.findAll()).extracting(op -> op.getStatus().name())
                .containsExactlyInAnyOrder("SUCCESS", "FAILED");
    }

    @Test
    void concurrentUpdateShouldRespectUniqueConstraints() throws Exception {
        HumanBeingFormDto firstForm = createForm();
        firstForm.setName("Update Hero A");
        firstForm.setSoundtrackName("Update Track A");
        var first = humanBeingService.create(firstForm);

        HumanBeingFormDto secondForm = createForm();
        secondForm.setName("Update Hero B");
        secondForm.setSoundtrackName("Update Track B");
        secondForm.setImpactSpeed(700);
        var second = humanBeingService.create(secondForm);

        HumanBeingFormDto targetForm = createForm();
        targetForm.setName("Merged Hero");
        targetForm.setSoundtrackName("Merged Theme");
        targetForm.setImpactSpeed(900);

        CountDownLatch startLatch = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Callable<Throwable> updateFirst = () -> updateHumanBeingConcurrently(startLatch, first.getId(), targetForm);
        Callable<Throwable> updateSecond = () -> updateHumanBeingConcurrently(startLatch, second.getId(), targetForm);

        List<Future<Throwable>> results = List.of(
                executorService.submit(updateFirst),
                executorService.submit(updateSecond)
        );

        startLatch.countDown();

        long success = 0;
        long uniquenessFailures = 0;
        for (Future<Throwable> future : results) {
            Throwable result = future.get();
            if (result == null) {
                success++;
            } else if (result instanceof HumanBeingUniquenessException) {
                uniquenessFailures++;
            } else {
                throw new AssertionError("Unexpected exception", result);
            }
        }
        executorService.shutdown();

        assertThat(success).isEqualTo(1);
        assertThat(uniquenessFailures).isEqualTo(1);
        assertThat(humanBeingRepository.count()).isEqualTo(2);
        long mergedCount = humanBeingRepository.count(
                HumanBeingSpecifications.hasNameAndSoundtrack("Merged Hero", "Merged Theme"));
        assertThat(mergedCount).isEqualTo(1);
    }

    @Test
    void importAndCreateInParallelShouldAvoidDuplicates() throws Exception {
        CountDownLatch startLatch = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Callable<Throwable> createTask = () -> createHumanBeingConcurrently(startLatch);
        Callable<Throwable> importTask = () -> importHumansConcurrently(startLatch);

        List<Future<Throwable>> results = List.of(
                executorService.submit(createTask),
                executorService.submit(importTask)
        );

        startLatch.countDown();

        long success = 0;
        long uniquenessFailures = 0;
        for (Future<Throwable> future : results) {
            Throwable result = future.get();
            if (result == null) {
                success++;
            } else if (result instanceof HumanBeingUniquenessException) {
                uniquenessFailures++;
            } else {
                throw new AssertionError("Unexpected exception", result);
            }
        }
        executorService.shutdown();

        assertThat(success).isEqualTo(1);
        assertThat(uniquenessFailures).isEqualTo(1);
        assertThat(humanBeingRepository.count()).isEqualTo(1);
        assertThat(importOperationRepository.count()).isEqualTo(1);
        ImportOperation operation = importOperationRepository.findAll().get(0);
        if (operation.getStatus() == ImportOperation.Status.FAILED) {
            assertThat(operation.getErrorMessage()).isNotBlank();
        } else {
            assertThat(operation.getStatus()).isEqualTo(ImportOperation.Status.SUCCESS);
        }
    }

    private Throwable createHumanBeingConcurrently(CountDownLatch startLatch) {
        try {
            startLatch.await();
            humanBeingService.create(createForm());
            return null;
        } catch (Throwable throwable) {
            return throwable;
        }
    }

    private Throwable importHumansConcurrently(CountDownLatch startLatch) {
        try {
            startLatch.await();
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "parallel.json",
                    "application/json",
                    buildSingleRecordJson().getBytes(StandardCharsets.UTF_8)
            );
            humanImportService.importHumans(file);
            return null;
        } catch (Throwable throwable) {
            return throwable;
        }
    }

    private Throwable updateHumanBeingConcurrently(CountDownLatch startLatch, Long id, HumanBeingFormDto form) {
        try {
            startLatch.await();
            humanBeingService.update(id, form);
            return null;
        } catch (Throwable throwable) {
            return throwable;
        }
    }

    private HumanBeingFormDto createForm() {
        HumanBeingFormDto form = new HumanBeingFormDto();
        form.setName("Parallel Hero");
        form.setCoordinatesX(10);
        form.setCoordinatesY(5.5f);
        form.setRealHero(Boolean.TRUE);
        form.setHasToothpick(Boolean.TRUE);
        form.setImpactSpeed(500);
        form.setSoundtrackName("Parallel Theme");
        form.setWeaponType(WeaponType.KNIFE);
        form.setMood(Mood.GLOOM);
        return form;
    }

    private String buildSingleRecordJson() {
        return "[" +
                "{\"name\":\"Parallel Hero\"," +
                "\"coordinates\":{\"x\":10,\"y\":5.5}," +
                "\"realHero\":true," +
                "\"hasToothpick\":true," +
                "\"impactSpeed\":500," +
                "\"soundtrackName\":\"Parallel Theme\"," +
                "\"weaponType\":\"KNIFE\"," +
                "\"mood\":\"GLOOM\"," +
                "\"car\":{\"name\":\"Parallel Car\",\"cool\":true}" +
                "}" +
                "]";
    }

    static class DataSourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            List<String> properties = new ArrayList<>();
            properties.add("spring.datasource.url=jdbc:h2:mem:uniquenessdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
            properties.add("spring.jpa.hibernate.ddl-auto=validate");
            properties.add("spring.flyway.enabled=true");
            properties.add("spring.flyway.locations=classpath:db/migration");
            properties.add("app.testdata.enabled=false");
            properties.add("spring.jpa.open-in-view=false");
            properties.add("spring.jpa.show-sql=false");
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                    properties.toArray(new String[0]));
        }
    }
}
