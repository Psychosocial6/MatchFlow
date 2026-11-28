package ru.matchflow.utils;

import org.springframework.stereotype.Component;
import ru.matchflow.dto.TeamResponse;
import ru.matchflow.entities.Team;

@Component
public class TeamMapper {
    public TeamResponse fromEntity(Team team) {
        return new TeamResponse(
                team.getId(),
                team.getCaptain().getId(),
                team.getName(),
                team.getName(),
                team.getCreationDate()
        );
    }
}
