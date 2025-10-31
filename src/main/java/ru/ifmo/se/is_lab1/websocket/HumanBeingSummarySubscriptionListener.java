package ru.ifmo.se.is_lab1.websocket;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import ru.ifmo.se.is_lab1.service.HumanBeingService;
import ru.ifmo.se.is_lab1.service.event.HumanBeingEventPublisher;

@Component
public class HumanBeingSummarySubscriptionListener {

    private static final String SUMMARY_DESTINATION = "/topic/humans-summary";

    private final HumanBeingService humanBeingService;
    private final HumanBeingEventPublisher eventPublisher;

    public HumanBeingSummarySubscriptionListener(HumanBeingService humanBeingService,
                                                 HumanBeingEventPublisher eventPublisher) {
        this.humanBeingService = humanBeingService;
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public void handleSubscription(SessionSubscribeEvent event) {
        String destination = (String) event.getMessage().getHeaders().get(SimpMessageHeaderAccessor.DESTINATION_HEADER);
        if (SUMMARY_DESTINATION.equals(destination)) {
            eventPublisher.publishSummary(humanBeingService.getSummary());
        }
    }
}
