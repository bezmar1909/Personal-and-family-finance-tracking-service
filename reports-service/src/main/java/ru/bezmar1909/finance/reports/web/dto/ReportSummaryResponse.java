package ru.bezmar1909.finance.reports.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ReportSummaryResponse(
        LocalDate from,
        LocalDate to,
        Long groupId,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance,
        int operationsCount,
        List<GroupedAmount> byCategory,
        List<GroupedAmount> byUser
) {
    public record GroupedAmount(String key, BigDecimal income, BigDecimal expense, BigDecimal balance) {
    }
}
