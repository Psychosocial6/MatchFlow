package ru.matchflow.services;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.matchflow.dto.ApplicationRequest;
import ru.matchflow.dto.ApplicationResponse;
import ru.matchflow.entities.Application;
import ru.matchflow.entities.Tournament;
import ru.matchflow.exceptions.custom.ApplicationNotFoundException;
import ru.matchflow.exceptions.custom.TournamentNotFoundException;
import ru.matchflow.repositories.ApplicationRepository;
import ru.matchflow.repositories.TournamentRepository;
import ru.matchflow.utils.ApplicationMapper;

@RequiredArgsConstructor
@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationMapper applicationMapper;
    private final TournamentRepository tournamentRepository;

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

    public ApplicationResponse getApplicationById(Integer id) {
        return applicationMapper.fromEntity(
                applicationRepository.findById(id)
                        .orElseThrow(() -> new ApplicationNotFoundException("Application not found"))
        );
    }

    public void approveApplication(Integer id) {
        applicationRepository.moderateApplication(id, true);
    }

    public void rejectApplication(Integer id) {
        applicationRepository.moderateApplication(id, false);
    }

    public void deleteApplication(Integer id, Integer userId) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found"));
        if (application.getTournament().getStatus() == Tournament.Status.REGISTRATION) {
            if (application.getParticipant() != null && userId.equals(application.getParticipant().getId())) {
                applicationRepository.deleteById(id);
                return;
            }
            else if (application.getTeam() != null && userId.equals(application.getTeam().getCaptain().getId())) {
                applicationRepository.deleteById(id);
                return;
            }
        }
        throw new IllegalArgumentException();
    }
}
