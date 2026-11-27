package ru.matchflow.services;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.WebUtils;
import ru.matchflow.config.VkAuthProperties;
import ru.matchflow.dto.LoginResponse;
import ru.matchflow.dto.VkAttemptRequest;
import ru.matchflow.dto.VkConfigResponse;
import ru.matchflow.dto.VkLoginRequest;
import ru.matchflow.dto.VkLoginResponse;
import ru.matchflow.dto.VkRegistrationRequest;
import ru.matchflow.entities.UserAccount;
import ru.matchflow.repositories.UserRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class VkAuthService {
    private static final String ATTEMPT_ATTRIBUTE = VkAuthService.class.getName() + ".attempt";
    private static final String REGISTRATION_ATTRIBUTE = VkAuthService.class.getName() + ".registration";
    private static final Duration ATTEMPT_TTL = Duration.ofMinutes(10);
    private static final Base64.Encoder BASE64_URL = Base64.getUrlEncoder().withoutPadding();

    private final VkAuthProperties properties;
    private final VkIdClient vkIdClient;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final SecureRandom random = new SecureRandom();

    public VkConfigResponse getConfig() {
        if (properties.configured()) {
            return new VkConfigResponse(true, properties.clientId(), properties.redirectUri());
        }
        return new VkConfigResponse(false, null, null);
    }

    public void start(HttpSession session, VkAttemptRequest request, Integer userId) {
        requireConfiguration();
        if (request.link() && userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sign in before linking VK ID");
        }

        Integer linkUserId = request.link() ? userId : null;
        synchronized (WebUtils.getSessionMutex(session)) {
            session.setAttribute(ATTEMPT_ATTRIBUTE, new Attempt(
                    request.state(), request.codeChallenge(), linkUserId, Instant.now().plus(ATTEMPT_TTL)));
        }
    }

    public VkLoginResponse login(HttpSession session, VkLoginRequest request, Integer currentUserId) {
        requireConfiguration();
        Attempt attempt = consumeAttempt(session, request, currentUserId);
        String vkId = vkIdClient.exchange(request);
        if (attempt.linkUserId() != null) {
            authService.linkVk(attempt.linkUserId(), vkId);
            return VkLoginResponse.builder().status(VkLoginResponse.Status.LINKED).build();
        }

        return userRepository.findByVkId(vkId)
                .map(this::loginExisting)
                .orElseGet(() -> startRegistration(session, vkId));
    }

    public LoginResponse register(HttpSession session, VkRegistrationRequest request) {
        requireConfiguration();
        if (session == null) {
            throw invalidRegistration();
        }
        synchronized (WebUtils.getSessionMutex(session)) {
            PendingRegistration registration = (PendingRegistration) session.getAttribute(REGISTRATION_ATTRIBUTE);
            if (registration == null || !registration.expiresAt().isAfter(Instant.now())
                    || !registration.ticket().equals(request.registrationTicket())) {
                throw invalidRegistration();
            }

            LoginResponse response = authService.registerWithVk(registration.vkId(), request);
            session.removeAttribute(REGISTRATION_ATTRIBUTE);
            return response;
        }
    }

    private Attempt consumeAttempt(HttpSession session, VkLoginRequest request, Integer currentUserId) {
        if (session == null) {
            throw invalidAttempt();
        }
        synchronized (WebUtils.getSessionMutex(session)) {
            Attempt attempt = (Attempt) session.getAttribute(ATTEMPT_ATTRIBUTE);
            if (attempt == null || !attempt.expiresAt().isAfter(Instant.now())
                    || !attempt.state().equals(request.state())
                    || !attempt.challenge().equals(challengeFor(request.codeVerifier()))) {
                throw invalidAttempt();
            }
            if (attempt.linkUserId() != null && !Objects.equals(attempt.linkUserId(), currentUserId)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sign in again to link VK ID");
            }

            session.removeAttribute(ATTEMPT_ATTRIBUTE);
            return attempt;
        }
    }

    private VkLoginResponse loginExisting(UserAccount user) {
        return VkLoginResponse.builder()
                .status(VkLoginResponse.Status.SIGNED_IN)
                .login(authService.issueLogin(user.getId(), user.getRole()))
                .build();
    }

    private VkLoginResponse startRegistration(HttpSession session, String vkId) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String ticket = BASE64_URL.encodeToString(bytes);
        synchronized (WebUtils.getSessionMutex(session)) {
            session.setAttribute(REGISTRATION_ATTRIBUTE,
                    new PendingRegistration(vkId, ticket, Instant.now().plus(ATTEMPT_TTL)));
        }

        return VkLoginResponse.builder()
                .status(VkLoginResponse.Status.REGISTRATION_REQUIRED)
                .registrationTicket(ticket)
                .build();
    }

    private void requireConfiguration() {
        if (!properties.configured()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "VK ID is not configured");
        }
    }

    private static String challengeFor(String verifier) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(verifier.getBytes(StandardCharsets.US_ASCII));
            return BASE64_URL.encodeToString(digest);
        } catch (NoSuchAlgorithmException failure) {
            throw new IllegalStateException("SHA-256 is unavailable", failure);
        }
    }

    private static ResponseStatusException invalidAttempt() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "VK login attempt is invalid or expired");
    }

    private static ResponseStatusException invalidRegistration() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "VK registration is invalid or expired");
    }

    private record Attempt(String state, String challenge, Integer linkUserId, Instant expiresAt) {
    }

    private record PendingRegistration(String vkId, String ticket, Instant expiresAt) {
    }
}
