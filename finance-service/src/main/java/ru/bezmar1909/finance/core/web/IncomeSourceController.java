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
import ru.bezmar1909.finance.core.service.IncomeSourceService;
import ru.bezmar1909.finance.core.web.dto.CreateIncomeSourceRequest;
import ru.bezmar1909.finance.core.web.dto.IncomeSourceResponse;

@RestController
@RequestMapping("/api/income-sources")
public class IncomeSourceController {
    private final IncomeSourceService sourceService;

    public IncomeSourceController(IncomeSourceService sourceService) {
        this.sourceService = sourceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IncomeSourceResponse create(@Valid @RequestBody CreateIncomeSourceRequest request, Authentication authentication) {
        return IncomeSourceResponse.from(sourceService.create(request.name(), request.groupId(), CurrentUser.id(authentication)));
    }

    @GetMapping
    public List<IncomeSourceResponse> list(@RequestParam(required = false) Long groupId, Authentication authentication) {
        return sourceService.list(groupId, CurrentUser.id(authentication)).stream()
                .map(IncomeSourceResponse::from)
                .toList();
    }
}
