package ru.bezmar1909.finance.auth.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.bezmar1909.finance.auth.domain.UserAccount;
import ru.bezmar1909.finance.auth.service.AuthService;
import ru.bezmar1909.finance.auth.service.JwtService;
import ru.bezmar1909.finance.auth.web.dto.AuthResponse;
import ru.bezmar1909.finance.auth.web.dto.LoginRequest;
import ru.bezmar1909.finance.auth.web.dto.RegisterRequest;
import ru.bezmar1909.finance.auth.web.dto.UserResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        UserAccount user = authService.register(request.email(), request.password(), request.fullName());
        return new AuthResponse(jwtService.createToken(user), UserResponse.from(user));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request.email(), request.password());
        return new AuthResponse(token, null);
    }
}
