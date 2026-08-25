package ru.bezmar1909.finance.core.repo;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.bezmar1909.finance.core.domain.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    @EntityGraph(attributePaths = "group")
    List<Category> findByUserIdAndGroupIsNull(Long userId);

    @EntityGraph(attributePaths = "group")
    List<Category> findByGroupId(Long groupId);
}
