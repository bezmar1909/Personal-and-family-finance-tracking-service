package ru.bezmar1909.finance.reports.web;

import org.springframework.security.core.Authentication;
import ru.bezmar1909.finance.reports.service.AccessToken;

public final class CurrentUser {
    private CurrentUser() {
    }

    public static Long id(Authentication authentication) {
        return ((AccessToken) authentication.getPrincipal()).userId();
    }

    public static String role(Authentication authentication) {
        return ((AccessToken) authentication.getPrincipal()).role();
    }
}
