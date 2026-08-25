package ru.bezmar1909.finance.reports.client;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FinanceOperationDto(
        Long id,
        BigDecimal amount,
        LocalDate operationDate,
        String type,
        String description,
        Long userId,
        Long categoryId,
        String categoryName,
        Long incomeSourceId,
        String incomeSourceName,
        Long groupId
) {
}
