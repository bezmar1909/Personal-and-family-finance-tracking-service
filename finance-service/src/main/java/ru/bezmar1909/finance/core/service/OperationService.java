package ru.bezmar1909.finance.core.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bezmar1909.finance.core.domain.Category;
import ru.bezmar1909.finance.core.domain.FamilyGroup;
import ru.bezmar1909.finance.core.domain.FinanceOperation;
import ru.bezmar1909.finance.core.repo.FinanceOperationRepository;
import ru.bezmar1909.finance.core.web.dto.CreateOperationRequest;

@Service
public class OperationService {
    private final FinanceOperationRepository operations;
    private final GroupService groupService;
    private final CategoryService categoryService;

    public OperationService(FinanceOperationRepository operations, GroupService groupService, CategoryService categoryService) {
        this.operations = operations;
        this.groupService = groupService;
        this.categoryService = categoryService;
    }

    @Transactional
    public FinanceOperation create(CreateOperationRequest request, Long userId) {
        FamilyGroup group = request.groupId() == null ? null : groupService.requireMember(request.groupId(), userId);
        Category category = categoryService.requireAccessible(request.categoryId(), request.groupId(), userId);
        if (category.getType() != request.type()) {
            throw new IllegalArgumentException("Operation type must match category type");
        }
        String description = request.description() == null ? "" : request.description();
        return operations.save(new FinanceOperation(
                request.amount(),
                request.operationDate(),
                request.type(),
                description,
                userId,
                category,
                group
        ));
    }

    @Transactional(readOnly = true)
    public List<FinanceOperation> list(Long groupId, Long userId) {
        if (groupId == null) {
            return operations.findByUserIdAndGroupIsNullOrderByOperationDateDesc(userId);
        }
        groupService.requireMember(groupId, userId);
        return operations.findByGroupIdOrderByOperationDateDesc(groupId);
    }

    @Transactional(readOnly = true)
    public List<FinanceOperation> internalReportData(LocalDate from, LocalDate to, Long groupId, List<Long> userIds, Long actorUserId) {
        if (groupId != null) {
            groupService.requireMember(groupId, actorUserId);
        } else if (userIds != null && userIds.stream().anyMatch(userId -> !userId.equals(actorUserId))) {
            throw new AccessDeniedException("Personal reports can include only current user data");
        }
        return operations.findByOperationDateBetween(from, to).stream()
                .filter(operation -> {
                    if (groupId == null) {
                        return operation.getGroup() == null && operation.getUserId().equals(actorUserId);
                    }
                    return operation.getGroup() != null && groupId.equals(operation.getGroup().getId());
                })
                .filter(operation -> userIds == null || userIds.isEmpty() || userIds.contains(operation.getUserId()))
                .toList();
    }
}
