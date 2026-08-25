package ru.bezmar1909.finance.core.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bezmar1909.finance.core.domain.FamilyGroup;

public interface FamilyGroupRepository extends JpaRepository<FamilyGroup, Long> {
}
