package ru.bezmar1909.finance.reports.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import ru.bezmar1909.finance.reports.client.FinanceClient;
import ru.bezmar1909.finance.reports.client.FinanceOperationDto;
import ru.bezmar1909.finance.reports.web.dto.ExpenseAnalyticsResponse;
import ru.bezmar1909.finance.reports.web.dto.ExpenseAnalyticsResponse.CategoryShare;
import ru.bezmar1909.finance.reports.web.dto.ExpenseAnalyticsResponse.MonthlyTrend;
import ru.bezmar1909.finance.reports.web.dto.ReportSummaryResponse;
import ru.bezmar1909.finance.reports.web.dto.ReportSummaryResponse.GroupedAmount;

@Service
public class ReportService {
    private final FinanceClient financeClient;

    public ReportService(FinanceClient financeClient) {
        this.financeClient = financeClient;
    }

    public ReportSummaryResponse summary(Long actorUserId, LocalDate from, LocalDate to, Long groupId, List<Long> userIds) {
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("Report end date must be after start date");
        }
        List<FinanceOperationDto> operations = financeClient.operations(actorUserId, from, to, groupId, userIds);
        BigDecimal income = sum(operations, "INCOME");
        BigDecimal expense = sum(operations, "EXPENSE");
        return new ReportSummaryResponse(
                from,
                to,
                groupId,
                income,
                expense,
                income.subtract(expense),
                operations.size(),
                grouped(operations, FinanceOperationDto::categoryName),
                grouped(operations.stream()
                        .filter(operation -> "INCOME".equals(operation.type()))
                        .toList(), operation -> operation.incomeSourceName() == null ? "Unknown" : operation.incomeSourceName()),
                grouped(operations, operation -> String.valueOf(operation.userId()))
        );
    }

    public ReportSummaryResponse monthly(Long actorUserId, int year, int month, Long groupId, List<Long> userIds) {
        YearMonth period = YearMonth.of(year, month);
        return summary(actorUserId, period.atDay(1), period.atEndOfMonth(), groupId, userIds);
    }

    public ReportSummaryResponse quarterly(Long actorUserId, int year, int quarter, Long groupId, List<Long> userIds) {
        if (quarter < 1 || quarter > 4) {
            throw new IllegalArgumentException("Quarter must be between 1 and 4");
        }
        Month firstMonth = Month.of((quarter - 1) * 3 + 1);
        LocalDate from = LocalDate.of(year, firstMonth, 1);
        LocalDate to = from.plusMonths(3).minusDays(1);
        return summary(actorUserId, from, to, groupId, userIds);
    }

    public ReportSummaryResponse yearly(Long actorUserId, int year, Long groupId, List<Long> userIds) {
        return summary(actorUserId, LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31), groupId, userIds);
    }

    public ExpenseAnalyticsResponse expenseAnalytics(Long actorUserId, LocalDate from, LocalDate to, Long groupId, List<Long> userIds) {
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("Report end date must be after start date");
        }
        List<FinanceOperationDto> operations = financeClient.operations(actorUserId, from, to, groupId, userIds);
        BigDecimal totalExpense = sum(operations, "EXPENSE");
        return new ExpenseAnalyticsResponse(
                from,
                to,
                groupId,
                totalExpense,
                categoryShares(operations, totalExpense),
                monthlyTrend(operations)
        );
    }

    public String csv(ReportSummaryResponse summary) {
        StringBuilder builder = new StringBuilder("from,to,groupId,totalIncome,totalExpense,balance,operationsCount\n");
        builder.append(summary.from()).append(',')
                .append(summary.to()).append(',')
                .append(summary.groupId() == null ? "" : summary.groupId()).append(',')
                .append(summary.totalIncome()).append(',')
                .append(summary.totalExpense()).append(',')
                .append(summary.balance()).append(',')
                .append(summary.operationsCount()).append("\n\n");
        builder.append("scope,key,income,expense,balance\n");
        summary.byCategory().forEach(row -> appendGroup(builder, "category", row));
        summary.byIncomeSource().forEach(row -> appendGroup(builder, "incomeSource", row));
        summary.byUser().forEach(row -> appendGroup(builder, "user", row));
        return builder.toString();
    }

    private BigDecimal sum(List<FinanceOperationDto> operations, String type) {
        return operations.stream()
                .filter(operation -> type.equals(operation.type()))
                .map(FinanceOperationDto::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<GroupedAmount> grouped(List<FinanceOperationDto> operations, java.util.function.Function<FinanceOperationDto, String> classifier) {
        Map<String, List<FinanceOperationDto>> grouped = operations.stream()
                .collect(java.util.stream.Collectors.groupingBy(classifier, LinkedHashMap::new, java.util.stream.Collectors.toList()));
        return grouped.entrySet().stream()
                .map(entry -> {
                    BigDecimal income = sum(entry.getValue(), "INCOME");
                    BigDecimal expense = sum(entry.getValue(), "EXPENSE");
                    return new GroupedAmount(entry.getKey(), income, expense, income.subtract(expense));
                })
                .sorted(Comparator.comparing(GroupedAmount::expense).reversed())
                .toList();
    }

    private List<CategoryShare> categoryShares(List<FinanceOperationDto> operations, BigDecimal totalExpense) {
        Map<String, List<FinanceOperationDto>> byCategory = operations.stream()
                .filter(operation -> "EXPENSE".equals(operation.type()))
                .collect(java.util.stream.Collectors.groupingBy(
                        FinanceOperationDto::categoryName,
                        LinkedHashMap::new,
                        java.util.stream.Collectors.toList()
                ));
        return byCategory.entrySet().stream()
                .map(entry -> {
                    BigDecimal amount = sum(entry.getValue(), "EXPENSE");
                    BigDecimal percent = totalExpense.signum() == 0
                            ? BigDecimal.ZERO
                            : amount.multiply(new BigDecimal("100")).divide(totalExpense, 2, RoundingMode.HALF_UP);
                    return new CategoryShare(entry.getKey(), amount, percent);
                })
                .sorted(Comparator.comparing(CategoryShare::amount).reversed())
                .toList();
    }

    private List<MonthlyTrend> monthlyTrend(List<FinanceOperationDto> operations) {
        Map<YearMonth, List<FinanceOperationDto>> byMonth = operations.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        operation -> YearMonth.from(operation.operationDate()),
                        LinkedHashMap::new,
                        java.util.stream.Collectors.toList()
                ));
        return byMonth.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    BigDecimal income = sum(entry.getValue(), "INCOME");
                    BigDecimal expense = sum(entry.getValue(), "EXPENSE");
                    return new MonthlyTrend(entry.getKey(), income, expense, income.subtract(expense));
                })
                .toList();
    }

    private void appendGroup(StringBuilder builder, String scope, GroupedAmount row) {
        builder.append(scope).append(',')
                .append(row.key()).append(',')
                .append(row.income()).append(',')
                .append(row.expense()).append(',')
                .append(row.balance()).append('\n');
    }
}
