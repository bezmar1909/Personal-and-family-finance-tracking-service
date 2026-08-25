package ru.bezmar1909.finance.auth.web.dto;

public record AuthResponse(String token, UserResponse user) {
}
