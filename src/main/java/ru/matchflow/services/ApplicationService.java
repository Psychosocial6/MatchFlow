package ru.matchflow.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.matchflow.dto.ApplicationRequest;
import ru.matchflow.dto.ApplicationResponse;
import ru.matchflow.exceptions.custom.ApplicationNotFoundException;
import ru.matchflow.repositories.ApplicationRepository;
import ru.matchflow.utils.ApplicationMapper;

@RequiredArgsConstructor
@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationMapper applicationMapper;

    @Transactional
    public ApplicationResponse createApplication(ApplicationRequest request) {
        applicationRepository.registerApplication(request.tournamentId(), request.userId(), request.teamId());
        if (request.userId() != null) {
            return applicationMapper.fromEntity(
                    applicationRepository.findTopByTournamentIdAndParticipantIdOrderByIdDesc(request.tournamentId(), request.userId())
                            .orElseThrow(() -> new ApplicationNotFoundException("Application not found"))
            );
        }
        return applicationMapper.fromEntity(
                applicationRepository.findTopByTournamentIdAndTeamIdOrderByIdDesc(request.tournamentId(), request.teamId())
                        .orElseThrow(() -> new ApplicationNotFoundException("Application not found"))
        );
    }
}
