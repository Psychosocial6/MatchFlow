package ru.matchflow.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.CodePointLength;

public record TeamRequest(
        @NotBlank
        @CodePointLength(max = 255)
        String name,

        @CodePointLength(min = 2, max = 10)
        String tag
) {
}
