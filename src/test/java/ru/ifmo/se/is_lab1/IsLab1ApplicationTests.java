package ru.ifmo.se.is_lab1;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.context.annotation.Import;
import ru.ifmo.se.is_lab1.config.TestConfig;

@SpringBootTest(properties = "app.minio.enabled=false")
@Import(TestConfig.class)
class IsLab1ApplicationTests {

    @Test
    void contextLoads() {
    }

}
