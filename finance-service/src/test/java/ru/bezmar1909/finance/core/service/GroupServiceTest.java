package ru.bezmar1909.finance.core.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
}
