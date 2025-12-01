package ru.ifmo.se.is_lab1.websocket;

import jakarta.validation.Valid;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import ru.ifmo.se.is_lab1.dto.ImpactSpeedCountRequest;
import ru.ifmo.se.is_lab1.service.HumanBeingService;

@Controller
public class HumanBeingWebSocketController {

    private final HumanBeingService humanBeingService;

    public HumanBeingWebSocketController(HumanBeingService humanBeingService) {
        this.humanBeingService = humanBeingService;
    }

    @MessageMapping("/humans/summary")
    public void summary() {
    }

    @MessageMapping("/humans/impact-speed/count")
    @SendTo("/topic/humans-impact-count")
    public long countByImpactSpeed(@Valid ImpactSpeedCountRequest request) {
        return humanBeingService.countByImpactSpeedLessThan(request.getThreshold());
    }

    @MessageMapping("/humans/mood/gloom")
    @SendTo("/topic/humans-mood-gloom")
    public int makeEveryoneGloomy() {
        return humanBeingService.updateMoodToGloom();
    }

    @MessageMapping("/humans/cars/assign-default")
    @SendTo("/topic/humans-default-car")
    public int assignDefaultCar() {
        return humanBeingService.assignDefaultCarToHeroesWithoutCar();
    }
}
