package ru.matchflow.dto;

import lombok.Builder;
import ru.matchflow.entities.Team;

import java.time.LocalDateTime;

@Builder
public record TeamResponse(
        int id,
        int captainId,
        String name,
        String tag,
        LocalDateTime creationDate
) {
    public static TeamResponse fromEntity(Team team) {
        return builder()
                .id(team.getId())
                .captainId(team.getCaptainId())
                .name(team.getName())
                .tag(team.getTag())
                .creationDate(team.getCreationDate())
                .build();
    }
}
