package ru.ifmo.se.is_lab1.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.user-selection")
public class UserSelectionProperties {

    /**
     * Имя пользователя, которое будет использоваться по умолчанию,
     * если не было передано ни одного источника (cookie/параметр).
     */
    private String defaultUsername = "anonymous";

    /**
     * Имя cookie, в котором сохраняется выбранный пользователь.
     */
    private String usernameCookieName = "hb-username";

    /**
     * Имя cookie, в котором хранится флаг администратора.
     */
    private String adminCookieName = "hb-admin";

    /**
     * Значение флага администратора. Если оно присутствует в cookie или параметре, пользователь получает доступ к общей истории.
     */
    private String adminFlagValue = "admin";

    /**
     * Срок жизни cookie в днях.
     */
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
