package ru.ifmo.se.is_lab1.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import ru.ifmo.se.is_lab1.config.UserSelectionProperties;
import ru.ifmo.se.is_lab1.service.security.CurrentUserService;

@ControllerAdvice
public class CurrentUserModelAttributes {

    private final CurrentUserService currentUserService;
    private final UserSelectionProperties userSelectionProperties;

    public CurrentUserModelAttributes(CurrentUserService currentUserService,
                                     UserSelectionProperties userSelectionProperties) {
        this.currentUserService = currentUserService;
        this.userSelectionProperties = userSelectionProperties;
    }

    @ModelAttribute("currentUserName")
    public String currentUserName() {
        return currentUserService.getCurrentUsername();
    }

    @ModelAttribute("currentUserIsAdmin")
    public boolean currentUserIsAdmin() {
        return currentUserService.isAdmin();
    }

    @ModelAttribute("userSelectionAdminFlag")
    public String userSelectionAdminFlag() {
        return userSelectionProperties.getAdminFlagValue();
    }
}
