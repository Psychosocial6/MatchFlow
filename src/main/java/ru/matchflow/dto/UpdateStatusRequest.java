package ru.matchflow.dto;

import jakarta.validation.constraints.NotNull;
import ru.matchflow.entities.Tournament;

public record UpdateStatusRequest(
        @NotNull
        Tournament.Status status
) {
}
