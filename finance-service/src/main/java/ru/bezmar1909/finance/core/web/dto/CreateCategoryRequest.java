package ru.bezmar1909.finance.core.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.bezmar1909.finance.core.domain.OperationType;

public record CreateCategoryRequest(
        @NotBlank @Size(max = 80) String name,
        @NotNull OperationType type,
        Long groupId
) {
}
