package ru.matchflow.dto;

import lombok.Builder;
import ru.matchflow.entities.Tournament;

import java.time.LocalDateTime;

@Builder
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
    public static TournamentResponse fromEntity(Tournament tournament) {
        return builder()
                .id(tournament.getId())
                .adminId(tournament.getAdminId())
                .sportId(tournament.getSportId())
                .title(tournament.getTitle())
                .regulations(tournament.getRegulations())
                .participationType(tournament.getParticipationType())
                .format(tournament.getFormat())
                .status(tournament.getStatus())
                .regStartDate(tournament.getRegStartDate())
                .regEndDate(tournament.getRegEndDate())
                .startDate(tournament.getStartDate())
                .endDate(tournament.getEndDate())
                .maxParticipants(tournament.getMaxParticipants())
                .build();
    }
}
