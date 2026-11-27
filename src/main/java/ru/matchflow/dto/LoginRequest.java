package ru.matchflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.CodePointLength;

public record LoginRequest(
        @NotBlank
        @CodePointLength(max = 255)
        String username,

        @NotEmpty
        @Size(max = 72)
        String password
) {
}
