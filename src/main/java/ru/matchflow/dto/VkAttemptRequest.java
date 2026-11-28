package ru.matchflow.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record VkAttemptRequest(
        @NotNull @Pattern(regexp = "[A-Za-z0-9_-]{43}") String state,
        @NotNull @Pattern(regexp = "[A-Za-z0-9_-]{43}") String codeChallenge,
        boolean link
) {
}
