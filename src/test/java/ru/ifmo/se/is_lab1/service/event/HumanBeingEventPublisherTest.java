package ru.ifmo.se.is_lab1.service.event;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import ru.ifmo.se.is_lab1.dto.HumanBeingSummary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class HumanBeingEventPublisherTest {

    private final SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
    private final HumanBeingEventPublisher publisher = new HumanBeingEventPublisher(messagingTemplate);

    @Test
    void publishShouldSendEventToHumansTopic() {
        HumanBeingEvent event = new HumanBeingEvent(HumanBeingEventType.CREATED, null);

        publisher.publish(event);

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/humans"), payloadCaptor.capture());
        assertThat(payloadCaptor.getValue()).isSameAs(event);
    }

    @Test
    void publishSummaryShouldSendSummaryToSummaryTopic() {
        HumanBeingSummary summary = new HumanBeingSummary(10, 50);

        publisher.publishSummary(summary);

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/humans-summary"), payloadCaptor.capture());
        assertThat(payloadCaptor.getValue()).isSameAs(summary);
    }
}
