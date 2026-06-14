package ru.bezmar1909.finance.core.web.dto;

import ru.bezmar1909.finance.core.domain.Category;
import ru.bezmar1909.finance.core.domain.OperationType;

public record CategoryResponse(Long id, String name, OperationType type, Long groupId) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getType(),
                category.getGroup() == null ? null : category.getGroup().getId()
        );
    }
}
