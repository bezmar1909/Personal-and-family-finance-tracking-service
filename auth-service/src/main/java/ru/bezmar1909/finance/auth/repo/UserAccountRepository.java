package ru.bezmar1909.finance.auth.repo;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.bezmar1909.finance.auth.domain.UserAccount;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
