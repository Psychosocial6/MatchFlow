package ru.matchflow.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.matchflow.dto.TeamRequest;
import ru.matchflow.dto.TeamResponse;
import ru.matchflow.entities.Team;
import ru.matchflow.entities.UserAccount;
import ru.matchflow.repositories.TeamRepository;
import ru.matchflow.repositories.UserRepository;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    @Transactional
    public TeamResponse createTeam(TeamRequest request, int captainId) {
        UserAccount captain = userRepository.getReferenceById(captainId);
        Team team = Team.builder()
                .captain(captain)
                .name(request.name())
                .tag(request.tag())
                .members(new HashSet<>(Set.of(captain)))
                .build();

        return TeamResponse.fromEntity(teamRepository.save(team));
    }
}
