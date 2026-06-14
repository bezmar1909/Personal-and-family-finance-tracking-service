package ru.bezmar1909.finance.auth.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import ru.bezmar1909.finance.auth.domain.Role;
import ru.bezmar1909.finance.auth.domain.UserAccount;

class JwtServiceTest {
    @Test
    void createsAndParsesAccessToken() {
        JwtService jwtService = new JwtService("test-secret-long-enough", 3600);
        UserAccount user = new UserAccount("user@example.com", "hash", "User Name", Role.USER);
        ReflectionTestUtils.setField(user, "id", 42L);

        AccessToken token = jwtService.parse(jwtService.createToken(user));

        assertThat(token.userId()).isEqualTo(42L);
        assertThat(token.email()).isEqualTo("user@example.com");
        assertThat(token.role()).isEqualTo("USER");
    }
}
