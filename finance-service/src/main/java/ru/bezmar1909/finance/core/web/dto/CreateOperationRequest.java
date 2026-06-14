package ru.bezmar1909.finance.core.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import ru.bezmar1909.finance.core.domain.OperationType;

public record CreateOperationRequest(
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotNull LocalDate operationDate,
        @NotNull OperationType type,
        @Size(max = 300) String description,
        @NotNull Long categoryId,
        Long incomeSourceId,
        Long groupId
) {
}
