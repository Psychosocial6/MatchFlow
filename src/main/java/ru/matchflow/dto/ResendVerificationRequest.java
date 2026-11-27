package ru.matchflow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.CodePointLength;

public record ResendVerificationRequest(
        @NotBlank @Email @CodePointLength(max = 255) String email
) {
}
