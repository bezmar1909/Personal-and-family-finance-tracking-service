package ru.bezmar1909.finance.core.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bezmar1909.finance.core.domain.FamilyGroup;
import ru.bezmar1909.finance.core.domain.IncomeSource;
import ru.bezmar1909.finance.core.repo.IncomeSourceRepository;

@Service
public class IncomeSourceService {
    private final IncomeSourceRepository sources;
    private final GroupService groupService;

    public IncomeSourceService(IncomeSourceRepository sources, GroupService groupService) {
        this.sources = sources;
        this.groupService = groupService;
    }

    @Transactional
    public IncomeSource create(String name, Long groupId, Long userId) {
        FamilyGroup group = groupId == null ? null : groupService.requireMember(groupId, userId);
        return sources.save(new IncomeSource(name, userId, group));
    }

    @Transactional(readOnly = true)
    public List<IncomeSource> list(Long groupId, Long userId) {
        if (groupId == null) {
            return sources.findByUserIdAndGroupIsNull(userId);
        }
        groupService.requireMember(groupId, userId);
        return sources.findByGroupId(groupId);
    }

    @Transactional(readOnly = true)
    public IncomeSource requireAccessible(Long sourceId, Long groupId, Long userId) {
        IncomeSource source = sources.findById(sourceId)
                .orElseThrow(() -> new IllegalArgumentException("Income source not found"));
        Long sourceGroupId = source.getGroup() == null ? null : source.getGroup().getId();
        if (groupId == null && (sourceGroupId != null || !source.getUserId().equals(userId))) {
            throw new IllegalArgumentException("Income source does not belong to personal operations");
        }
        if (groupId != null && !groupId.equals(sourceGroupId)) {
            throw new IllegalArgumentException("Income source does not belong to this group");
        }
        return source;
    }
}
