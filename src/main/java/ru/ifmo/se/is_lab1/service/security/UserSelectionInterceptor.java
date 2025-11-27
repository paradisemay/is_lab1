package ru.ifmo.se.is_lab1.service.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.WebUtils;

import ru.ifmo.se.is_lab1.config.UserSelectionProperties;

@Component
public class UserSelectionInterceptor implements HandlerInterceptor {

    private final UserContextHolder userContextHolder;
    private final UserSelectionProperties properties;

    public UserSelectionInterceptor(UserContextHolder userContextHolder, UserSelectionProperties properties) {
        this.userContextHolder = userContextHolder;
        this.properties = properties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String usernameParam = request.getParameter("user");
        String adminParam = request.getParameter("admin");

        String username = resolveUsernameFromCookie(request);
        boolean admin = resolveAdminFromCookie(request);

        if (usernameParam != null) {
            username = StringUtils.hasText(usernameParam) ? usernameParam.trim() : properties.getDefaultUsername();
            persistCookie(response, properties.getUsernameCookieName(), username);
        }
        if (adminParam != null) {
            admin = properties.getAdminFlagValue().equals(adminParam);
            persistCookie(response, properties.getAdminCookieName(), admin ? properties.getAdminFlagValue() : "false");
        }

        userContextHolder.setCurrentUser(username, admin);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        userContextHolder.clear();
    }

    private String resolveUsernameFromCookie(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, properties.getUsernameCookieName());
        if (cookie != null && StringUtils.hasText(cookie.getValue())) {
            return cookie.getValue();
        }
        return properties.getDefaultUsername();
    }

    private boolean resolveAdminFromCookie(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, properties.getAdminCookieName());
        return cookie != null && properties.getAdminFlagValue().equals(cookie.getValue());
    }

    private void persistCookie(HttpServletResponse response, String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(false);
        cookie.setPath("/");
        cookie.setMaxAge((int) (properties.getCookieTtlDays() * 24L * 60L * 60L));
        response.addCookie(cookie);
    }
}
