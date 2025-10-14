package ru.ifmo.se.is_lab1.service.event;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class MusicBandEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public MusicBandEventPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publish(MusicBandEvent event) {
        messagingTemplate.convertAndSend("/topic/bands", event);
    }
}
