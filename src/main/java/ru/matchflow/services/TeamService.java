package ru.matchflow.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.matchflow.dto.TeamRequest;
import ru.matchflow.entities.Team;
import ru.matchflow.entities.UserAccount;
import ru.matchflow.repositories.TeamRepository;
import ru.matchflow.repositories.UserRepository;

@Service
public class TeamService {
    private final TeamRepository teams;
    private final UserRepository users;

    public TeamService(TeamRepository teams, UserRepository users) {
        this.teams = teams;
        this.users = users;
    }

    @Transactional
    public Team create(int captainId, TeamRequest request) {
        UserAccount captain = users.getReferenceById(captainId);
        return teams.save(request.toEntity(captainId, captain));
    }
}
