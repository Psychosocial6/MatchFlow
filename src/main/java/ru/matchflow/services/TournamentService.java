package ru.matchflow.services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.matchflow.dto.TournamentRequest;
import ru.matchflow.dto.TournamentResponse;
import ru.matchflow.entities.Tournament;
import ru.matchflow.repositories.TournamentRepository;

@Service
@RequiredArgsConstructor
public class TournamentService {

    private final TournamentRepository tournamentRepository;

    @Transactional
    public TournamentResponse createTournament(TournamentRequest request, int adminId) {
        Tournament tournament = Tournament.builder()
                .adminId(adminId)
                .sportId(request.sportId())
                .title(request.title())
                .regulations(request.regulations())
                .participationType(request.participationType())
                .format(request.format())
                .regStartDate(request.regStartDate())
                .regEndDate(request.regEndDate())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .maxParticipants(request.maxParticipants())
                .build();

        validateDates(tournament);
        return TournamentResponse.fromEntity(tournamentRepository.save(tournament));
    }

    @Transactional(readOnly = true)
    public TournamentResponse getTournamentById(int id) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tournament not found"));
        return TournamentResponse.fromEntity(tournament);
    }

    private void validateDates(Tournament tournament) {
        if (!tournament.getRegStartDate().isBefore(tournament.getRegEndDate())
                || tournament.getRegEndDate().isAfter(tournament.getStartDate())
                || tournament.getStartDate().isAfter(tournament.getEndDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Dates must satisfy regStartDate < regEndDate <= startDate <= endDate");
        }
    }
}
