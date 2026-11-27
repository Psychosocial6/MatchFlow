package ru.matchflow.dto;

import ru.matchflow.entities.Tournament;

import java.time.LocalDateTime;

public record TournamentResponse(
        int id,
        int adminId,
        int sportId,
        String title,
        String regulations,
        Tournament.ParticipationType participationType,
        Tournament.Format format,
        Tournament.Status status,
        LocalDateTime regStartDate,
        LocalDateTime regEndDate,
        LocalDateTime startDate,
        LocalDateTime endDate,
        int maxParticipants
) {
}
