package ru.ifmo.se.is_lab1.websocket;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import ru.ifmo.se.is_lab1.dto.MusicBandSummary;
import ru.ifmo.se.is_lab1.service.MusicBandService;

@Controller
public class MusicBandWebSocketController {

    private final MusicBandService musicBandService;

    public MusicBandWebSocketController(MusicBandService musicBandService) {
        this.musicBandService = musicBandService;
    }

    @MessageMapping("/bands/summary")
    @SendTo("/topic/bands-summary")
    public MusicBandSummary summary() {
        return new MusicBandSummary(musicBandService.findAll().size(), musicBandService.sumImpactSpeed());
    }
}
