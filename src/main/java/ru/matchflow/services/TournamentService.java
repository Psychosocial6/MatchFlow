package ru.matchflow.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.matchflow.dto.TournamentRequest;
import ru.matchflow.entities.Tournament;
import ru.matchflow.repositories.TournamentRepository;

@Service
public class TournamentService {
    private final TournamentRepository tournaments;

    public TournamentService(TournamentRepository tournaments) {
        this.tournaments = tournaments;
    }

    @Transactional
    public Tournament create(int adminId, TournamentRequest request) {
        Tournament tournament = request.toEntity(adminId);
        if (!tournament.getRegStartDate().isBefore(tournament.getRegEndDate())
                || tournament.getRegEndDate().isAfter(tournament.getStartDate())
                || tournament.getStartDate().isAfter(tournament.getEndDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Dates must satisfy regStartDate < regEndDate <= startDate <= endDate");
        }
        return tournaments.save(tournament);
    }

    @Transactional(readOnly = true)
    public Tournament getById(int id) {
        return tournaments.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tournament not found"));
    }
}
