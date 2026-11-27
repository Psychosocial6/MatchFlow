package ru.matchflow.services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.matchflow.dto.LoginRequest;
import ru.matchflow.dto.LoginResponse;
import ru.matchflow.security.AccountUserDetails;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at most 72 bytes");
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));
        } catch (BadCredentialsException ignored) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        AccountUserDetails account = (AccountUserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(account.getUserId(), account.getRole());
        return LoginResponse.of(token, jwtService.getTtlSeconds());
    }
}
