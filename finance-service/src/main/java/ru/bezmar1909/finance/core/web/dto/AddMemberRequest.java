package ru.bezmar1909.finance.core.web.dto;

import jakarta.validation.constraints.NotNull;

public record AddMemberRequest(@NotNull Long userId) {
}
