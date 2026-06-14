package ru.bezmar1909.finance.core.service;

import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bezmar1909.finance.core.domain.FamilyGroup;
import ru.bezmar1909.finance.core.domain.GroupMember;
import ru.bezmar1909.finance.core.domain.MemberRole;
import ru.bezmar1909.finance.core.repo.FamilyGroupRepository;
import ru.bezmar1909.finance.core.repo.GroupMemberRepository;

@Service
public class GroupService {
    private final FamilyGroupRepository groups;
    private final GroupMemberRepository members;

    public GroupService(FamilyGroupRepository groups, GroupMemberRepository members) {
        this.groups = groups;
        this.members = members;
    }

    @Transactional
    public FamilyGroup create(String name, Long ownerUserId) {
        FamilyGroup group = groups.save(new FamilyGroup(name, ownerUserId));
        members.save(new GroupMember(group, ownerUserId, MemberRole.OWNER));
        return group;
    }

    @Transactional
    public void addMember(Long groupId, Long actorUserId, Long memberUserId) {
        GroupMember actor = members.findByGroupIdAndUserId(groupId, actorUserId)
                .orElseThrow(() -> new AccessDeniedException("No access to this group"));
        if (actor.getRole() != MemberRole.OWNER) {
            throw new AccessDeniedException("Only group owner can manage members");
        }
        if (!members.existsByGroupIdAndUserId(groupId, memberUserId)) {
            FamilyGroup group = groups.findById(groupId).orElseThrow(() -> new IllegalArgumentException("Group not found"));
            members.save(new GroupMember(group, memberUserId, MemberRole.MEMBER));
        }
    }

    @Transactional(readOnly = true)
    public List<FamilyGroup> list(Long userId) {
        return members.findGroupsByUserId(userId);
    }

    @Transactional(readOnly = true)
    public FamilyGroup requireMember(Long groupId, Long userId) {
        if (!members.existsByGroupIdAndUserId(groupId, userId)) {
            throw new AccessDeniedException("No access to this group");
        }
        return groups.findById(groupId).orElseThrow(() -> new IllegalArgumentException("Group not found"));
    }
}
