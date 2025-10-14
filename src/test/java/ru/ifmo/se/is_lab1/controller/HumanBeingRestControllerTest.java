package ru.ifmo.se.is_lab1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.se.is_lab1.dto.HumanBeingFormDto;
import ru.ifmo.se.is_lab1.model.Car;
import ru.ifmo.se.is_lab1.model.Mood;
import ru.ifmo.se.is_lab1.model.WeaponType;
import ru.ifmo.se.is_lab1.repository.LegacyCarRepository;
import ru.ifmo.se.is_lab1.service.event.HumanBeingEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class HumanBeingRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LegacyCarRepository carRepository;

    @MockBean
    private HumanBeingEventPublisher eventPublisher;

    private Long carId;

    @BeforeEach
    void setUp() {
        carId = carRepository.save(new Car("Lada", true)).getId();
    }

    @Test
    void createAndFetchHuman() throws Exception {
        HumanBeingFormDto form = new HumanBeingFormDto();
        form.setName("Testing Human");
        form.setCoordinateX(10.0);
        form.setCoordinateY(20.0);
        form.setRealHero(true);
        form.setHasToothpick(Boolean.TRUE);
        form.setImpactSpeed(3.5);
        form.setSoundtrackName("Testing Soundtrack");
        form.setMinutesOfWaiting(12L);
        form.setMood(Mood.CALM);
        form.setWeaponType(WeaponType.BAT);
        form.setCarId(carId);

        String response = mockMvc.perform(post("/api/humans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/humans/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Testing Human"))
                .andExpect(jsonPath("$.car.name").value("Lada"));

        mockMvc.perform(get("/api/humans")
                        .param("name", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id));
    }

    @Test
    void validationErrorsAreReturned() throws Exception {
        HumanBeingFormDto form = new HumanBeingFormDto();
        form.setName("");
        form.setHasToothpick(null);
        form.setImpactSpeed(1000.0);
        form.setSoundtrackName("");
        form.setMinutesOfWaiting(null);

        mockMvc.perform(post("/api/humans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void bulkMoodUpdateChangesRecords() throws Exception {
        HumanBeingFormDto angry = new HumanBeingFormDto();
        angry.setName("Angry Human");
        angry.setCoordinateX(1.0);
        angry.setCoordinateY(2.0);
        angry.setRealHero(false);
        angry.setHasToothpick(Boolean.FALSE);
        angry.setImpactSpeed(4.0);
        angry.setSoundtrackName("Rage");
        angry.setMinutesOfWaiting(5L);
        angry.setMood(Mood.SORROW);
        mockMvc.perform(post("/api/humans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(angry)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/humans/mood/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"sourceMood\":\"SORROW\"," +
                                "\"targetMood\":\"RAGE\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        String body = mockMvc.perform(get("/api/humans")
                        .param("mood", Mood.RAGE.name()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(objectMapper.readTree(body).get("totalElements").asInt()).isEqualTo(1);
    }
}
