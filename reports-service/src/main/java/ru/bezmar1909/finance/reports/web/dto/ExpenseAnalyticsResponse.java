package ru.bezmar1909.finance.reports.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public record ExpenseAnalyticsResponse(
        LocalDate from,
        LocalDate to,
        Long groupId,
        BigDecimal totalExpense,
        List<CategoryShare> categoryShares,
        List<MonthlyTrend> monthlyTrend
) {
    public record CategoryShare(String categoryName, BigDecimal amount, BigDecimal percent) {
    }

    public record MonthlyTrend(YearMonth month, BigDecimal income, BigDecimal expense, BigDecimal balance) {
    }
}
