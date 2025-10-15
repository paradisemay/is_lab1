package ru.ifmo.se.is_lab1.service.event;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import ru.ifmo.se.is_lab1.dto.HumanBeingSummary;

@Component
public class HumanBeingEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public HumanBeingEventPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publish(HumanBeingEvent event) {
        messagingTemplate.convertAndSend("/topic/humans", event);
    }

    public void publishSummary(HumanBeingSummary summary) {
        messagingTemplate.convertAndSend("/topic/humans-summary", summary);
    }
}
