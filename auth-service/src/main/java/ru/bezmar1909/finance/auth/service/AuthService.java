package ru.bezmar1909.finance.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bezmar1909.finance.auth.domain.Role;
import ru.bezmar1909.finance.auth.domain.UserAccount;
import ru.bezmar1909.finance.auth.repo.UserAccountRepository;

@Service
public class AuthService {
    private final UserAccountRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserAccountRepository users, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserAccount register(String email, String password, String fullName) {
        if (users.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("User with this email already exists");
        }
        Role role = users.count() == 0 ? Role.ADMIN : Role.USER;
        return users.save(new UserAccount(
                email.toLowerCase(),
                passwordEncoder.encode(password),
                fullName,
                role
        ));
    }

    @Transactional(readOnly = true)
    public String login(String email, String password) {
        UserAccount user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        return jwtService.createToken(user);
    }
}
