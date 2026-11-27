package ru.matchflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.CodePointLength;

public record VkRegistrationRequest(
        @NotNull @Pattern(regexp = "[A-Za-z0-9_-]{43}") String registrationTicket,
        @NotBlank @CodePointLength(max = 255) String username
) {
}
