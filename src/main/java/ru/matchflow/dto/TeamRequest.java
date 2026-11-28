package ru.matchflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Length;

import java.util.Set;

public record TeamRequest(
        @NotNull
        @Positive
        Integer captainId,
        @NotNull
        @NotBlank
        @Length(max = 255)
        String name,
        @Length(min = 2, max = 10)
        String tag,
        @NotNull
        Set<Integer> membersId
) {
}
