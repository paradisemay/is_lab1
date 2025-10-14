package ru.ifmo.se.is_lab1.service.event;

import ru.ifmo.se.is_lab1.dto.HumanBeingDto;

public class HumanBeingEvent {
    private final HumanBeingEventType type;
    private final HumanBeingDto human;

    public HumanBeingEvent(HumanBeingEventType type, HumanBeingDto human) {
        this.type = type;
        this.human = human;
    }

    public HumanBeingEventType getType() {
        return type;
    }

    public HumanBeingDto getHuman() {
        return human;
    }
}
