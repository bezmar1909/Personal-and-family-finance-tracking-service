package ru.bezmar1909.finance.core.repo;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.bezmar1909.finance.core.domain.IncomeSource;

public interface IncomeSourceRepository extends JpaRepository<IncomeSource, Long> {
    @EntityGraph(attributePaths = "group")
    List<IncomeSource> findByUserIdAndGroupIsNull(Long userId);

    @EntityGraph(attributePaths = "group")
    List<IncomeSource> findByGroupId(Long groupId);
}
