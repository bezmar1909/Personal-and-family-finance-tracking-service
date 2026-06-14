package ru.bezmar1909.finance.core.web;

import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.bezmar1909.finance.core.service.OperationService;
import ru.bezmar1909.finance.core.web.dto.OperationResponse;

@RestController
@RequestMapping("/internal/operations")
public class InternalOperationController {
    private final OperationService operationService;
    private final String internalToken;

    public InternalOperationController(OperationService operationService, @Value("${app.internal-token}") String internalToken) {
        this.operationService = operationService;
        this.internalToken = internalToken;
    }

    @GetMapping
    public List<OperationResponse> listForReport(
            @RequestHeader("X-Internal-Token") String token,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam Long actorUserId,
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) List<Long> userIds
    ) {
        if (!internalToken.equals(token)) {
            throw new AccessDeniedException("Invalid internal token");
        }
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("Report end date must be after start date");
        }
        return operationService.internalReportData(from, to, groupId, userIds, actorUserId).stream()
                .map(OperationResponse::from)
                .toList();
    }
}
