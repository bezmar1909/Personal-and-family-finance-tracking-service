package ru.bezmar1909.finance.core.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.AccessDeniedException;
import ru.bezmar1909.finance.core.domain.FamilyGroup;
import ru.bezmar1909.finance.core.domain.GroupMember;
import ru.bezmar1909.finance.core.domain.MemberRole;
import ru.bezmar1909.finance.core.repo.FamilyGroupRepository;
import ru.bezmar1909.finance.core.repo.GroupMemberRepository;

class GroupServiceTest {
    @Test
    void createAddsOwnerAsGroupMember() {
        FamilyGroupRepository groups = org.mockito.Mockito.mock(FamilyGroupRepository.class);
        GroupMemberRepository members = org.mockito.Mockito.mock(GroupMemberRepository.class);
        when(groups.save(any(FamilyGroup.class))).thenAnswer(invocation -> invocation.getArgument(0));
        GroupService service = new GroupService(groups, members);

        service.create("Family", 7L);

        ArgumentCaptor<GroupMember> captor = ArgumentCaptor.forClass(GroupMember.class);
        verify(members).save(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getUserId()).isEqualTo(7L);
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getRole()).isEqualTo(MemberRole.OWNER);
    }

    @Test
    void nonOwnerCannotAddMembers() {
        FamilyGroupRepository groups = org.mockito.Mockito.mock(FamilyGroupRepository.class);
        GroupMemberRepository members = org.mockito.Mockito.mock(GroupMemberRepository.class);
        FamilyGroup group = new FamilyGroup("Family", 1L);
        when(members.findByGroupIdAndUserId(10L, 2L))
                .thenReturn(Optional.of(new GroupMember(group, 2L, MemberRole.MEMBER)));
        GroupService service = new GroupService(groups, members);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.addMember(10L, 2L, 3L))
                .isInstanceOf(AccessDeniedException.class);
        verify(members, never()).save(any(GroupMember.class));
    }
}
