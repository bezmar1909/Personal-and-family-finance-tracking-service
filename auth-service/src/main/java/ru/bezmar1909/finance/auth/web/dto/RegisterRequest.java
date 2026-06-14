package ru.bezmar1909.finance.auth.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Email @NotBlank String email,
        @Size(min = 6, max = 80) String password,
        @NotBlank @Size(max = 120) String fullName
) {
}
