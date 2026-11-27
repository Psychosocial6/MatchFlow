package ru.matchflow.services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.matchflow.dto.AccountResponse;
import ru.matchflow.dto.LoginRequest;
import ru.matchflow.dto.LoginResponse;
import ru.matchflow.dto.RegistrationRequest;
import ru.matchflow.dto.RegistrationResponse;
import ru.matchflow.dto.VkRegistrationRequest;
import ru.matchflow.entities.UserAccount;
import ru.matchflow.entities.UserRole;
import ru.matchflow.repositories.UserRepository;
import ru.matchflow.security.AccountUserDetails;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;

    @Transactional
    public RegistrationResponse register(RegistrationRequest request) {
        validatePasswordLength(request.password());
        UserAccount user = UserAccount.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .emailVerificationRequired(true)
                .build();

        return savePendingRegistration(user);
    }

    @Transactional
    public LoginResponse registerWithVk(String vkId, VkRegistrationRequest request) {
        UserAccount user = UserAccount.builder()
                .username(request.username())
                .vkId(vkId)
                .build();

        UserAccount savedUser = userRepository.save(user);
        return issueLogin(savedUser.getId(), savedUser.getRole());
    }

    @Transactional
    public void linkVk(int userId, String vkId) {
        UserAccount user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account no longer exists"));
        if (user.isEmailVerificationPending()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Confirm your email before linking VK ID");
        }
        if (user.getVkId() != null && !user.getVkId().equals(vkId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This account already has a VK ID");
        }

        user.setVkId(vkId);
        userRepository.saveAndFlush(user);
    }

    public AccountResponse getAccount(int userId) {
        return userRepository.findById(userId)
                .map(AccountResponse::fromEntity)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account no longer exists"));
    }

    public LoginResponse login(LoginRequest request) {
        validatePasswordLength(request.password());
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));
        } catch (BadCredentialsException ignored) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        AccountUserDetails account = (AccountUserDetails) authentication.getPrincipal();
        return issueLogin(account.getUserId(), account.getRole());
    }

    LoginResponse issueLogin(int userId, UserRole role) {
        String token = jwtService.generateToken(userId, role);
        return LoginResponse.of(token, jwtService.getTtlSeconds());
    }

    private RegistrationResponse savePendingRegistration(UserAccount user) {
        UserAccount savedUser = userRepository.save(user);
        emailVerificationService.requestVerification(savedUser);
        return RegistrationResponse.fromEntity(savedUser);
    }

    private static void validatePasswordLength(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at most 72 bytes");
        }
    }
}
