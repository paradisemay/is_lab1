package ru.ifmo.se.is_lab1.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import ru.ifmo.se.is_lab1.config.TestConfig;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.transaction.annotation.Transactional;

import ru.ifmo.se.is_lab1.domain.ImportOperation;
import ru.ifmo.se.is_lab1.repository.CarRepository;
import ru.ifmo.se.is_lab1.repository.CoordinatesRepository;
import ru.ifmo.se.is_lab1.repository.HumanBeingRepository;
import ru.ifmo.se.is_lab1.repository.ImportOperationRepository;
import ru.ifmo.se.is_lab1.service.event.HumanBeingEventPublisher;
import ru.ifmo.se.is_lab1.service.security.UserContextHolder;

@SpringBootTest
@Transactional
@ContextConfiguration(initializers = HumanImportServiceIntegrationTest.DataSourceInitializer.class)
@Import(TestConfig.class)
class HumanImportServiceIntegrationTest {

    @Autowired
    private HumanImportService humanImportService;

    @Autowired
    private HumanBeingRepository humanBeingRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private CoordinatesRepository coordinatesRepository;

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
        userContextHolder.setCurrentUser("import-tester", false);
    }

    @AfterEach
    void tearDown() {
        userContextHolder.clear();
    }

    @Test
    void importsSampleFileFromProjectRoot() throws Exception {
        byte[] content = Files.readAllBytes(Path.of("import.json"));
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "import.json",
                "application/json",
                content);

        int imported = humanImportService.importHumans(file);

        assertThat(imported).isEqualTo(4);
        assertThat(humanBeingRepository.count()).isEqualTo(4);
        assertThat(carRepository.count()).isEqualTo(4);
        assertThat(coordinatesRepository.count()).isEqualTo(4);
        assertThat(importOperationRepository.count()).isEqualTo(1);

        ImportOperation operation = importOperationRepository.findAll().get(0);
        assertThat(operation.getStatus()).isEqualTo(ImportOperation.Status.SUCCESS);
        assertThat(operation.getAddedCount()).isEqualTo(4);
        assertThat(operation.getInitiator()).isEqualTo("import-tester");
    }

    static class DataSourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                    "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
                    "spring.jpa.hibernate.ddl-auto=update",
                    "spring.flyway.enabled=false",
                    "app.testdata.enabled=false",
                    "app.minio.enabled=false");
        }
    }
}
