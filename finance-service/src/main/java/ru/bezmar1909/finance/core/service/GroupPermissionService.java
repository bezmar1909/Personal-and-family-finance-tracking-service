package ru.bezmar1909.finance.core.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import ru.bezmar1909.finance.core.domain.MemberRole;

@Service("groupPermissions")
public class GroupPermissionService {
    private final GroupService groupService;

    public GroupPermissionService(GroupService groupService) {
        this.groupService = groupService;
    }

    public boolean canManageMembers(Long groupId, Authentication authentication) {
        AccessToken token = (AccessToken) authentication.getPrincipal();
        return "ADMIN".equals(token.role()) || groupService.memberRole(groupId, token.userId()) == MemberRole.OWNER;
    }
}
