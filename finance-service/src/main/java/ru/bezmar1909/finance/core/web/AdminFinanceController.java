package ru.bezmar1909.finance.core.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.bezmar1909.finance.core.repo.CategoryRepository;
import ru.bezmar1909.finance.core.repo.FamilyGroupRepository;
import ru.bezmar1909.finance.core.repo.FinanceOperationRepository;
import ru.bezmar1909.finance.core.repo.GroupMemberRepository;
import ru.bezmar1909.finance.core.repo.IncomeSourceRepository;

@RestController
@RequestMapping("/api/admin/finance")
public class AdminFinanceController {
    private final FamilyGroupRepository groups;
    private final GroupMemberRepository members;
    private final CategoryRepository categories;
    private final IncomeSourceRepository incomeSources;
    private final FinanceOperationRepository operations;

    public AdminFinanceController(FamilyGroupRepository groups, GroupMemberRepository members,
                                  CategoryRepository categories, IncomeSourceRepository incomeSources,
                                  FinanceOperationRepository operations) {
        this.groups = groups;
        this.members = members;
        this.categories = categories;
        this.incomeSources = incomeSources;
        this.operations = operations;
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminFinanceStats stats() {
        return new AdminFinanceStats(groups.count(), members.count(), categories.count(), incomeSources.count(), operations.count());
    }

    public record AdminFinanceStats(long groups, long members, long categories, long incomeSources, long operations) {
    }
}
