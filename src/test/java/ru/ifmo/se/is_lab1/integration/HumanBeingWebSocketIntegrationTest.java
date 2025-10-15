package ru.ifmo.se.is_lab1.integration;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import ru.ifmo.se.is_lab1.dto.HumanBeingDto;
import ru.ifmo.se.is_lab1.dto.HumanBeingFormDto;
import ru.ifmo.se.is_lab1.model.Mood;
import ru.ifmo.se.is_lab1.model.WeaponType;
import ru.ifmo.se.is_lab1.repository.CarRepository;
import ru.ifmo.se.is_lab1.repository.CoordinatesRepository;
import ru.ifmo.se.is_lab1.repository.HumanBeingRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HumanBeingWebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private HumanBeingRepository humanBeingRepository;

    @Autowired
    private CoordinatesRepository coordinatesRepository;

    @Autowired
    private CarRepository carRepository;

    private WebSocketStompClient stompClient;

    @BeforeEach
    void setUp() {
        humanBeingRepository.deleteAll();
        carRepository.deleteAll();
        coordinatesRepository.deleteAll();
        List<Transport> transports = List.of(new WebSocketTransport(new StandardWebSocketClient()));
        SockJsClient sockJsClient = new SockJsClient(transports);
        stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @AfterEach
    void tearDown() {
        if (stompClient != null) {
            stompClient.stop();
        }
    }

    @Test
    void websocketPublishesEventAndSummaryAfterCreate() throws Exception {
        CountDownLatch summaryLatch = new CountDownLatch(2);
        CountDownLatch eventLatch = new CountDownLatch(1);
        List<Map<String, Object>> summaries = new CopyOnWriteArrayList<>();
        AtomicReference<Map<String, Object>> eventRef = new AtomicReference<>();

        StompSession session = stompClient.connectAsync(String.format("ws://localhost:%d/ws", port), new StompSessionHandlerAdapter() {})
                .get(5, TimeUnit.SECONDS);

        session.subscribe("/topic/humans-summary", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Map.class;
            }

            @Override
            @SuppressWarnings("unchecked")
            public void handleFrame(StompHeaders headers, Object payload) {
                summaries.add((Map<String, Object>) payload);
                summaryLatch.countDown();
            }
        });

        session.subscribe("/topic/humans", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Map.class;
            }

            @Override
            @SuppressWarnings("unchecked")
            public void handleFrame(StompHeaders headers, Object payload) {
                eventRef.set((Map<String, Object>) payload);
                eventLatch.countDown();
            }
        });

        session.send("/app/humans/summary", new byte[0]);

        HumanBeingFormDto form = new HumanBeingFormDto();
        form.setName("Ivy");
        form.setCoordinatesX(15);
        form.setCoordinatesY(3.5f);
        form.setRealHero(Boolean.TRUE);
        form.setHasToothpick(Boolean.TRUE);
        form.setImpactSpeed(220);
        form.setSoundtrackName("Starlight");
        form.setWeaponType(WeaponType.KNIFE);
        form.setMood(Mood.SADNESS);

        ResponseEntity<HumanBeingDto> response = restTemplate.postForEntity("/api/humans", form, HumanBeingDto.class);
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        Assertions.assertThat(eventLatch.await(5, TimeUnit.SECONDS)).isTrue();
        Assertions.assertThat(summaryLatch.await(5, TimeUnit.SECONDS)).isTrue();

        Assertions.assertThat(summaries).hasSize(2);
        Map<String, Object> initialSummary = summaries.get(0);
        Map<String, Object> updatedSummary = summaries.get(1);

        Assertions.assertThat(((Number) initialSummary.get("totalCount")).longValue()).isEqualTo(0L);
        Assertions.assertThat(((Number) updatedSummary.get("totalCount")).longValue()).isEqualTo(1L);
        Assertions.assertThat(((Number) updatedSummary.get("totalImpactSpeed")).longValue()).isEqualTo(220L);

        Map<String, Object> eventPayload = eventRef.get();
        Assertions.assertThat(eventPayload).isNotNull();
        Assertions.assertThat(eventPayload.get("type")).isEqualTo("CREATED");
        @SuppressWarnings("unchecked")
        Map<String, Object> human = (Map<String, Object>) eventPayload.get("human");
        Assertions.assertThat(human).isNotNull();
        Assertions.assertThat(human.get("name")).isEqualTo("Ivy");

        session.disconnect();
    }
}
