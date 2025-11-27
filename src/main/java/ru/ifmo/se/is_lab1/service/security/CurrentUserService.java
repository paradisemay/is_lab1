package ru.ifmo.se.is_lab1.service.security;

import org.springframework.stereotype.Component;

@Component
public class CurrentUserService {

    private final UserContextHolder userContextHolder;

    public CurrentUserService(UserContextHolder userContextHolder) {
        this.userContextHolder = userContextHolder;
    }

    public String getCurrentUsername() {
        return userContextHolder.getCurrentUser().username();
    }

    public boolean isAdmin() {
        return userContextHolder.getCurrentUser().admin();
    }
}
