package ru.matchflow.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.matchflow.dto.LoginResponse;
import ru.matchflow.dto.VkAttemptRequest;
import ru.matchflow.dto.VkConfigResponse;
import ru.matchflow.dto.VkLoginRequest;
import ru.matchflow.dto.VkLoginResponse;
import ru.matchflow.dto.VkRegistrationRequest;
import ru.matchflow.services.VkAuthService;

@RestController
@RequestMapping("/api/auth/vk")
@RequiredArgsConstructor
public class VkAuthController {
    private final VkAuthService vkAuthService;

    @GetMapping("/config")
    public ResponseEntity<VkConfigResponse> config() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(vkAuthService.getConfig());
    }

    @PostMapping(path = "/attempts", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void start(@Valid @RequestBody VkAttemptRequest body, @AuthenticationPrincipal Jwt principal,
                      HttpServletRequest request) {
        vkAuthService.start(request.getSession(), body, userId(principal));
    }

    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VkLoginResponse> login(@Valid @RequestBody VkLoginRequest body,
                                                @AuthenticationPrincipal Jwt principal, HttpServletRequest request) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(vkAuthService.login(request.getSession(false), body, userId(principal)));
    }

    @PostMapping(path = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody VkRegistrationRequest body,
                                                  HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .cacheControl(CacheControl.noStore())
                .body(vkAuthService.register(request.getSession(false), body));
    }

    private static Integer userId(Jwt principal) {
        return principal == null ? null : Integer.valueOf(principal.getSubject());
    }
}
