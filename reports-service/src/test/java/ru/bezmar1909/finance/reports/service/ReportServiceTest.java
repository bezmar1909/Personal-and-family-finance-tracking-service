package ru.bezmar1909.finance.reports.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import ru.bezmar1909.finance.reports.client.FinanceClient;
import ru.bezmar1909.finance.reports.client.FinanceOperationDto;

class ReportServiceTest {
    @Test
    void summaryCalculatesIncomeExpenseAndBalance() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 1, 31);
        FinanceClient client = new FinanceClient(null, "", "") {
            @Override
            public List<FinanceOperationDto> operations(Long actorUserId, LocalDate from, LocalDate to, Long groupId, List<Long> userIds) {
                return List.of(
                        new FinanceOperationDto(1L, new BigDecimal("1000.00"), from, "INCOME", "Salary", 1L, 1L, "Salary", null),
                        new FinanceOperationDto(2L, new BigDecimal("250.00"), from, "EXPENSE", "Food", 1L, 2L, "Food", null)
                );
            }
        };

        var summary = new ReportService(client).summary(1L, from, to, null, null);

        assertThat(summary.totalIncome()).isEqualByComparingTo("1000.00");
        assertThat(summary.totalExpense()).isEqualByComparingTo("250.00");
        assertThat(summary.balance()).isEqualByComparingTo("750.00");
        assertThat(summary.operationsCount()).isEqualTo(2);
    }

    @Test
    void expenseAnalyticsCalculatesCategorySharesAndMonthlyTrend() {
        LocalDate january = LocalDate.of(2026, 1, 10);
        LocalDate february = LocalDate.of(2026, 2, 10);
        FinanceClient client = new FinanceClient(null, "", "") {
            @Override
            public List<FinanceOperationDto> operations(Long actorUserId, LocalDate from, LocalDate to, Long groupId, List<Long> userIds) {
                return List.of(
                        new FinanceOperationDto(1L, new BigDecimal("1000.00"), january, "INCOME", "Salary", 1L, 1L, "Salary", null),
                        new FinanceOperationDto(2L, new BigDecimal("300.00"), january, "EXPENSE", "Food", 1L, 2L, "Food", null),
                        new FinanceOperationDto(3L, new BigDecimal("700.00"), february, "EXPENSE", "Rent", 1L, 3L, "Rent", null)
                );
            }
        };

        var analytics = new ReportService(client).expenseAnalytics(1L, january, february, null, null);

        assertThat(analytics.totalExpense()).isEqualByComparingTo("1000.00");
        assertThat(analytics.categoryShares()).extracting("categoryName").containsExactly("Rent", "Food");
        assertThat(analytics.categoryShares().get(0).percent()).isEqualByComparingTo("70.00");
        assertThat(analytics.monthlyTrend()).hasSize(2);
    }
}
