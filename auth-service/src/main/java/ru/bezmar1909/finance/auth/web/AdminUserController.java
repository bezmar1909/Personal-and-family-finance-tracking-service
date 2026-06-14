package ru.bezmar1909.finance.auth.web;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.bezmar1909.finance.auth.repo.UserAccountRepository;
import ru.bezmar1909.finance.auth.web.dto.UserResponse;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final UserAccountRepository users;

    public AdminUserController(UserAccountRepository users) {
        this.users = users;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> list() {
        return users.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }
}
