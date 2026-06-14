package ru.bezmar1909.finance.core.web;

import org.springframework.security.core.Authentication;
import ru.bezmar1909.finance.core.service.AccessToken;

public final class CurrentUser {
    private CurrentUser() {
    }

    public static Long id(Authentication authentication) {
        return ((AccessToken) authentication.getPrincipal()).userId();
    }
}
