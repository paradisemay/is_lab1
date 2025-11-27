package ru.ifmo.se.is_lab1.service.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class UserSelectionInterceptor implements HandlerInterceptor {

    private static final String SESSION_USERNAME = "activeUsername";
    private static final String SESSION_ADMIN = "activeAdmin";

    private final UserContextHolder userContextHolder;

    public UserSelectionInterceptor(UserContextHolder userContextHolder) {
        this.userContextHolder = userContextHolder;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession session = request.getSession(true);

        String usernameParam = request.getParameter("user");
        String adminParam = request.getParameter("admin");

        String username = (String) session.getAttribute(SESSION_USERNAME);
        Boolean admin = (Boolean) session.getAttribute(SESSION_ADMIN);

        if (usernameParam != null) {
            username = usernameParam.trim();
            session.setAttribute(SESSION_USERNAME, username);
        }
        if (adminParam != null) {
            admin = Boolean.parseBoolean(adminParam);
            session.setAttribute(SESSION_ADMIN, admin);
        }

        if (username == null) {
            username = "anonymous";
            session.setAttribute(SESSION_USERNAME, username);
        }
        if (admin == null) {
            admin = Boolean.FALSE;
            session.setAttribute(SESSION_ADMIN, admin);
        }

        userContextHolder.setCurrentUser(username, admin);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        userContextHolder.clear();
    }
}
