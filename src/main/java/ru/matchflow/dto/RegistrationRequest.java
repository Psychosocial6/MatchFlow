package ru.matchflow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.CodePointLength;

public record RegistrationRequest(
        @NotBlank @CodePointLength(max = 255) String username,
        @NotBlank @Email @CodePointLength(max = 255) String email,
        @NotBlank @Size(max = 72) String password
) {
}
