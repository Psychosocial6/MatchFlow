package ru.matchflow.utils;

import org.springframework.stereotype.Component;
import ru.matchflow.dto.TournamentResponse;
import ru.matchflow.entities.Tournament;

@Component
public class TournamentMapper {
    public TournamentResponse fromEntity(Tournament tournament) {
        return new TournamentResponse(
                tournament.getId(),
                tournament.getAdmin().getId(),
                tournament.getSport().getId(),
                tournament.getTitle(),
                tournament.getRegulations(),
                tournament.getParticipationType(),
                tournament.getFormat(),
                tournament.getStatus(),
                tournament.getRegStartDate(),
                tournament.getRegEndDate(),
                tournament.getStartDate(),
                tournament.getEndDate(),
                tournament.getMaxParticipants()
        );
    }
}
