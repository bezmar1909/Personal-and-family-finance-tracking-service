package ru.bezmar1909.finance.core.web;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.bezmar1909.finance.core.service.OperationService;
import ru.bezmar1909.finance.core.web.dto.CreateOperationRequest;
import ru.bezmar1909.finance.core.web.dto.OperationResponse;

@RestController
@RequestMapping("/api/operations")
public class OperationController {
    private final OperationService operationService;

    public OperationController(OperationService operationService) {
        this.operationService = operationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OperationResponse create(@Valid @RequestBody CreateOperationRequest request, Authentication authentication) {
        return OperationResponse.from(operationService.create(request, CurrentUser.id(authentication)));
    }

    @GetMapping
    public List<OperationResponse> list(@RequestParam(required = false) Long groupId, Authentication authentication) {
        return operationService.list(groupId, CurrentUser.id(authentication)).stream()
                .map(OperationResponse::from)
                .toList();
    }
}
