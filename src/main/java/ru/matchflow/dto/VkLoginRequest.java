package ru.matchflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VkLoginRequest(
        @NotBlank @Size(max = 4096) String code,
        @NotBlank @Size(max = 512) String deviceId,
        @NotNull @Pattern(regexp = "[A-Za-z0-9_-]{43}") String state,
        @NotNull @Pattern(regexp = "[A-Za-z0-9_-]{43,128}") String codeVerifier
) {
}
