package ru.bezmar1909.finance.core.repo;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.bezmar1909.finance.core.domain.FinanceOperation;

public interface FinanceOperationRepository extends JpaRepository<FinanceOperation, Long> {
    @EntityGraph(attributePaths = {"category", "group"})
    List<FinanceOperation> findByUserIdAndGroupIsNullOrderByOperationDateDesc(Long userId);

    @EntityGraph(attributePaths = {"category", "group"})
    List<FinanceOperation> findByGroupIdOrderByOperationDateDesc(Long groupId);

    @EntityGraph(attributePaths = {"category", "group"})
    List<FinanceOperation> findByOperationDateBetween(LocalDate from, LocalDate to);
}
