package ru.bezmar1909.finance.core.web.dto;

import ru.bezmar1909.finance.core.domain.FamilyGroup;

public record GroupResponse(Long id, String name, Long ownerUserId) {
    public static GroupResponse from(FamilyGroup group) {
        return new GroupResponse(group.getId(), group.getName(), group.getOwnerUserId());
    }
}
