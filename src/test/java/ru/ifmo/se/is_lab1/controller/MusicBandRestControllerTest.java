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
import ru.ifmo.se.is_lab1.domain.Mood;
import ru.ifmo.se.is_lab1.dto.MusicBandFormDto;
import ru.ifmo.se.is_lab1.service.CarService;
import ru.ifmo.se.is_lab1.service.event.MusicBandEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MusicBandRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CarService carService;

    @MockBean
    private MusicBandEventPublisher eventPublisher;

    private Long carId;

    @BeforeEach
    void setUp() {
        carId = carService.create("Lada", "2107", "Синий").getId();
    }

    @Test
    void createAndFetchBand() throws Exception {
        MusicBandFormDto form = new MusicBandFormDto();
        form.setName("Testing Band");
        form.setImpactSpeed(new BigDecimal("3.50"));
        form.setSoundtrackName("Testing Soundtrack");
        form.setMood(Mood.CALM);
        form.setCarId(carId);

        String response = mockMvc.perform(post("/api/bands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var created = objectMapper.readTree(response);
        long id = created.get("id").asLong();

        mockMvc.perform(get("/api/bands/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Testing Band"))
                .andExpect(jsonPath("$.car.name").value("Lada"));

        mockMvc.perform(get("/api/bands")
                        .param("soundtrackPrefix", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id));
    }

    @Test
    void validationErrorsAreReturned() throws Exception {
        MusicBandFormDto form = new MusicBandFormDto();
        form.setName("");
        form.setImpactSpeed(new BigDecimal("-1"));
        form.setSoundtrackName("");
        form.setMood(null);

        mockMvc.perform(post("/api/bands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void bulkMoodUpdateChangesRecords() throws Exception {
        MusicBandFormDto angry = new MusicBandFormDto();
        angry.setName("Angry Band");
        angry.setImpactSpeed(new BigDecimal("4.00"));
        angry.setSoundtrackName("Rage");
        angry.setMood(Mood.ANGRY);
        mockMvc.perform(post("/api/bands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(angry)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/bands/mood/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"sourceMood\":\"ANGRY\"," +
                                "\"targetMood\":\"HAPPY\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        String body = mockMvc.perform(get("/api/bands")
                        .param("mood", Mood.HAPPY.name()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(objectMapper.readTree(body).get("totalElements").asInt()).isEqualTo(1);
    }
}
