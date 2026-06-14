package ru.bezmar1909.finance.core.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import ru.bezmar1909.finance.core.domain.FinanceOperation;
import ru.bezmar1909.finance.core.domain.OperationType;

public record OperationResponse(
        Long id,
        BigDecimal amount,
        LocalDate operationDate,
        OperationType type,
        String description,
        Long userId,
        Long categoryId,
        String categoryName,
        Long groupId
) {
    public static OperationResponse from(FinanceOperation operation) {
        return new OperationResponse(
                operation.getId(),
                operation.getAmount(),
                operation.getOperationDate(),
                operation.getType(),
                operation.getDescription(),
                operation.getUserId(),
                operation.getCategory().getId(),
                operation.getCategory().getName(),
                operation.getGroup() == null ? null : operation.getGroup().getId()
        );
    }
}
