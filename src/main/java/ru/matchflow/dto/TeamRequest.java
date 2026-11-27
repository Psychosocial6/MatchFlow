package ru.matchflow.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.CodePointLength;
import ru.matchflow.entities.Team;
import ru.matchflow.entities.UserAccount;

import java.util.HashSet;
import java.util.Set;

public record TeamRequest(
        @NotBlank @CodePointLength(max = 255) String name,
        @CodePointLength(min = 2, max = 10) String tag
) {
    public Team toEntity(int captainId, UserAccount captain) {
        return Team.builder()
                .captainId(captainId)
                .name(name)
                .tag(tag)
                .members(new HashSet<>(Set.of(captain)))
                .build();
    }
}
