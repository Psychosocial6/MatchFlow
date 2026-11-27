package ru.matchflow.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.matchflow.dto.AccountResponse;
import ru.matchflow.dto.LoginRequest;
import ru.matchflow.dto.LoginResponse;
import ru.matchflow.dto.RegistrationRequest;
import ru.matchflow.dto.RegistrationResponse;
import ru.matchflow.dto.ResendVerificationRequest;
import ru.matchflow.services.AuthService;
import ru.matchflow.services.EmailVerificationService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam @Pattern(regexp = "[A-Za-z0-9_-]{43}") String token) {
        emailVerificationService.verifyEmail(token);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentType(MediaType.TEXT_PLAIN)
                .body("Email confirmed. You can sign in.");
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Void> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        emailVerificationService.resendVerification(request.email());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<AccountResponse> me(@AuthenticationPrincipal Jwt principal) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(authService.getAccount(Integer.parseInt(principal.getSubject())));
    }
}
