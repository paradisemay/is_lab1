package ru.ifmo.se.is_lab1.websocket;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import ru.ifmo.se.is_lab1.dto.HumanBeingSummary;
import ru.ifmo.se.is_lab1.service.HumanBeingService;

@Controller
public class HumanBeingWebSocketController {

    private final HumanBeingService humanBeingService;

    public HumanBeingWebSocketController(HumanBeingService humanBeingService) {
        this.humanBeingService = humanBeingService;
    }

    @MessageMapping("/humans/summary")
    @SendTo("/topic/humans-summary")
    public HumanBeingSummary summary() {
        return new HumanBeingSummary(humanBeingService.findAll().size(), humanBeingService.sumImpactSpeed());
    }
}
