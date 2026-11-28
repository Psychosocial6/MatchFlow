package ru.matchflow.dto;

import ru.matchflow.entities.Application;

import java.time.LocalDateTime;

public record ApplicationResponse(
        Integer id,
        Integer tournamentId,
        Integer userId,
        Integer teamId, //TODO: return tournament, user, team info if needed
        Application.Status status,
        LocalDateTime creationDate
) {
}
