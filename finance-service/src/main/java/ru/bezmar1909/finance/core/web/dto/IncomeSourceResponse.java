package ru.bezmar1909.finance.core.web.dto;

import ru.bezmar1909.finance.core.domain.IncomeSource;

public record IncomeSourceResponse(Long id, String name, Long groupId) {
    public static IncomeSourceResponse from(IncomeSource source) {
        return new IncomeSourceResponse(
                source.getId(),
                source.getName(),
                source.getGroup() == null ? null : source.getGroup().getId()
        );
    }
}
