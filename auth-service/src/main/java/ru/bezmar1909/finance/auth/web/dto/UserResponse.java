package ru.bezmar1909.finance.auth.web.dto;

import ru.bezmar1909.finance.auth.domain.UserAccount;

public record UserResponse(Long id, String email, String fullName, String role) {
    public static UserResponse from(UserAccount user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRole().name());
    }
}
