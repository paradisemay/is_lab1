package ru.ifmo.se.is_lab1.service.security;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class UserContextHolder {

    private static final ThreadLocal<UserContext> CONTEXT = new ThreadLocal<>();

    public void setCurrentUser(String username, boolean admin) {
        String value = StringUtils.hasText(username) ? username : "anonymous";
        CONTEXT.set(new UserContext(value, admin));
    }

    public UserContext getCurrentUser() {
        UserContext context = CONTEXT.get();
        if (context == null) {
            return new UserContext("anonymous", false);
        }
        return context;
    }

    public void clear() {
        CONTEXT.remove();
    }
}
