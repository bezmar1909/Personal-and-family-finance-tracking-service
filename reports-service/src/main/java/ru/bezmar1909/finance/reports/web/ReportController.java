package ru.bezmar1909.finance.reports.web;

import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.bezmar1909.finance.reports.service.ReportService;
import ru.bezmar1909.finance.reports.web.dto.ExpenseAnalyticsResponse;
import ru.bezmar1909.finance.reports.web.dto.ReportSummaryResponse;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/summary")
    public ReportSummaryResponse summary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) List<Long> userIds,
            Authentication authentication
    ) {
        return reportService.summary(CurrentUser.id(authentication), from, to, groupId, userIds);
    }

    @GetMapping("/expense-analytics")
    public ExpenseAnalyticsResponse expenseAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) List<Long> userIds,
            Authentication authentication
    ) {
        return reportService.expenseAnalytics(CurrentUser.id(authentication), from, to, groupId, userIds);
    }

    @GetMapping(value = "/summary.csv", produces = "text/csv")
    public ResponseEntity<String> summaryCsv(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) List<Long> userIds,
            Authentication authentication
    ) {
        ReportSummaryResponse summary = reportService.summary(CurrentUser.id(authentication), from, to, groupId, userIds);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=finance-report.csv")
                .body(reportService.csv(summary));
    }
}
