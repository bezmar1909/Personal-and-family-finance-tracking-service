package ru.bezmar1909.finance.auth.service;

public record AccessToken(Long userId, String email, String role) {
}
