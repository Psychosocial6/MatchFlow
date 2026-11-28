package ru.matchflow.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.matchflow.dto.TeamRequest;
import ru.matchflow.dto.TeamResponse;
import ru.matchflow.entities.Team;
import ru.matchflow.entities.UserAccount;
import ru.matchflow.exceptions.custom.UserNotFoundException;
import ru.matchflow.repositories.TeamRepository;
import ru.matchflow.repositories.UserRepository;
import ru.matchflow.utils.TeamMapper;

import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final TeamMapper teamMapper;

    @Transactional
    public TeamResponse createTeam(TeamRequest request) {
        HashSet<UserAccount> members = new HashSet<>();
        for (Integer id : request.membersId()) {
            members.add(resolveUser(id));
        }
        Team team = new Team(
            resolveUser(request.captainId()),
            request.name(),
            request.tag(),
            members
        );
        return teamMapper.fromEntity(teamRepository.save(team));
    }

    //TODO: add update/delete if needed

    private UserAccount resolveUser(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}
