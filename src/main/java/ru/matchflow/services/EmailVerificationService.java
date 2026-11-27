package ru.matchflow.services;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.matchflow.config.EmailVerificationProperties;
import ru.matchflow.entities.EmailVerificationToken;
import ru.matchflow.entities.UserAccount;
import ru.matchflow.events.EmailVerificationRequested;
import ru.matchflow.repositories.EmailVerificationTokenRepository;
import ru.matchflow.repositories.UserRepository;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailVerificationTokenService tokenService;
    private final EmailVerificationProperties properties;
    private final ApplicationEventPublisher events;

    @Transactional
    public void requestVerification(UserAccount user) {
        EmailVerificationToken token = EmailVerificationToken.builder()
                .user(user)
                .build();
        issueToken(token, Instant.now());
    }

    @Transactional
    public void resendVerification(String email) {
        userRepository.findByEmail(email)
                .flatMap(user -> tokenRepository.findByUserId(user.getId()))
                .ifPresent(token -> {
                    Instant now = Instant.now();
                    if (!token.getIssuedAt().plus(properties.resendInterval()).isAfter(now)) {
                        issueToken(token, now);
                    }
                });
    }

    @Transactional
    public void verifyEmail(String rawToken) {
        EmailVerificationToken token = tokenRepository.findByTokenHash(tokenService.hashToken(rawToken))
                .filter(candidate -> candidate.getExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Verification link is invalid or expired"));

        token.getUser().setEmailVerifiedAt(Instant.now());
        tokenRepository.delete(token);
    }

    private void issueToken(EmailVerificationToken token, Instant now) {
        String rawToken = tokenService.generateToken();

        token.setTokenHash(tokenService.hashToken(rawToken));
        token.setIssuedAt(now);
        token.setExpiresAt(now.plus(properties.tokenTtl()));
        tokenRepository.save(token);

        events.publishEvent(new EmailVerificationRequested(token.getUser().getEmail(), rawToken));
    }
}
