package ru.matchflow.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ApplicationRequest(
        @Positive
        @NotNull
        Integer tournamentId,
        @Positive
        Integer userId,
        @Positive
        Integer teamId
) {
}
