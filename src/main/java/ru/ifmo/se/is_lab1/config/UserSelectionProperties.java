package ru.ifmo.se.is_lab1.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.user-selection")
public class UserSelectionProperties {

    private String defaultUsername = "anonymous";

    private String usernameCookieName = "hb-username";

    private String adminCookieName = "hb-admin";

    private String adminFlagValue = "admin";

    private int cookieTtlDays = 30;

    public String getDefaultUsername() {
        return defaultUsername;
    }

    public void setDefaultUsername(String defaultUsername) {
        this.defaultUsername = defaultUsername;
    }

    public String getUsernameCookieName() {
        return usernameCookieName;
    }

    public void setUsernameCookieName(String usernameCookieName) {
        this.usernameCookieName = usernameCookieName;
    }

    public String getAdminCookieName() {
        return adminCookieName;
    }

    public void setAdminCookieName(String adminCookieName) {
        this.adminCookieName = adminCookieName;
    }

    public String getAdminFlagValue() {
        return adminFlagValue;
    }

    public void setAdminFlagValue(String adminFlagValue) {
        this.adminFlagValue = adminFlagValue;
    }

    public int getCookieTtlDays() {
        return cookieTtlDays;
    }

    public void setCookieTtlDays(int cookieTtlDays) {
        this.cookieTtlDays = cookieTtlDays;
    }
}
