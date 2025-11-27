package ru.ifmo.se.is_lab1.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import ru.ifmo.se.is_lab1.service.security.UserSelectionInterceptor;

@Configuration
public class UserSelectionConfiguration implements WebMvcConfigurer {

    private final UserSelectionInterceptor userSelectionInterceptor;

    @Autowired
    public UserSelectionConfiguration(UserSelectionInterceptor userSelectionInterceptor) {
        this.userSelectionInterceptor = userSelectionInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userSelectionInterceptor);
    }
}
