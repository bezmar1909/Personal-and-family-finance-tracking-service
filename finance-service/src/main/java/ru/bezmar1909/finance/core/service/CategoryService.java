package ru.bezmar1909.finance.core.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bezmar1909.finance.core.domain.Category;
import ru.bezmar1909.finance.core.domain.FamilyGroup;
import ru.bezmar1909.finance.core.domain.OperationType;
import ru.bezmar1909.finance.core.repo.CategoryRepository;

@Service
public class CategoryService {
    private final CategoryRepository categories;
    private final GroupService groupService;

    public CategoryService(CategoryRepository categories, GroupService groupService) {
        this.categories = categories;
        this.groupService = groupService;
    }

    @Transactional
    public Category create(String name, OperationType type, Long groupId, Long userId) {
        FamilyGroup group = groupId == null ? null : groupService.requireMember(groupId, userId);
        return categories.save(new Category(name, type, userId, group));
    }

    @Transactional(readOnly = true)
    public List<Category> list(Long groupId, Long userId) {
        if (groupId == null) {
            return categories.findByUserIdAndGroupIsNull(userId);
        }
        groupService.requireMember(groupId, userId);
        return categories.findByGroupId(groupId);
    }

    @Transactional(readOnly = true)
    public Category requireAccessible(Long categoryId, Long groupId, Long userId) {
        Category category = categories.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        Long categoryGroupId = category.getGroup() == null ? null : category.getGroup().getId();
        if (groupId == null && (categoryGroupId != null || !category.getUserId().equals(userId))) {
            throw new IllegalArgumentException("Category does not belong to personal operations");
        }
        if (groupId != null && !groupId.equals(categoryGroupId)) {
            throw new IllegalArgumentException("Category does not belong to this group");
        }
        return category;
    }
}
