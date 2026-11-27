package ru.matchflow.services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.matchflow.dto.TournamentRequest;
import ru.matchflow.dto.TournamentResponse;
import ru.matchflow.entities.Sport;
import ru.matchflow.entities.Tournament;
import ru.matchflow.entities.UserAccount;
import ru.matchflow.exceptions.custom.SportNotFoundException;
import ru.matchflow.exceptions.custom.UserNotFoundException;
import ru.matchflow.repositories.SportRepository;
import ru.matchflow.repositories.TournamentRepository;
import ru.matchflow.repositories.UserRepository;
import ru.matchflow.utils.TournamentMapper;

@Service
@RequiredArgsConstructor
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final SportRepository sportRepository;
    private final UserRepository userRepository;
    private final TournamentMapper tournamentMapper;

    @Transactional
    public TournamentResponse createTournament(TournamentRequest request, int adminId) {
        Tournament tournament = new Tournament(
                resolveUser(adminId),
                resolveSport(request.sportId()),
                request.title(),
                request.regulations(),
                request.participationType(),
                request.format(),
                Tournament.Status.DRAFT,
                request.regStartDate(),
                request.regEndDate(),
                request.startDate(),
                request.endDate(),
                request.maxParticipants()
        );

        validateDates(tournament);
        return tournamentMapper.fromEntity(tournamentRepository.save(tournament));
    }

    @Transactional(readOnly = true)
    public TournamentResponse getTournamentById(int id) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tournament not found"));
        return tournamentMapper.fromEntity(tournament);
    }

    private void validateDates(Tournament tournament) {
        if (!tournament.getRegStartDate().isBefore(tournament.getRegEndDate())
                || tournament.getRegEndDate().isAfter(tournament.getStartDate())
                || tournament.getStartDate().isAfter(tournament.getEndDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Dates must satisfy regStartDate < regEndDate <= startDate <= endDate");
        }
    }

    private UserAccount resolveUser(int id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private Sport resolveSport(int id) {
        return sportRepository.findById(id)
                .orElseThrow(() -> new SportNotFoundException("Sport not found"));
    }
}
