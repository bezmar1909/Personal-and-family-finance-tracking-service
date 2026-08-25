package ru.bezmar1909.finance.core.repo;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.bezmar1909.finance.core.domain.FamilyGroup;
import ru.bezmar1909.finance.core.domain.GroupMember;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    boolean existsByGroupIdAndUserId(Long groupId, Long userId);

    Optional<GroupMember> findByGroupIdAndUserId(Long groupId, Long userId);

    List<GroupMember> findByUserId(Long userId);

    @Query("select m.group from GroupMember m where m.userId = :userId order by m.group.id")
    List<FamilyGroup> findGroupsByUserId(@Param("userId") Long userId);
}
