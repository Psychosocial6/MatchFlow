package ru.matchflow.dto;

import java.time.LocalDateTime;

public record TeamResponse(
        int id,
        int captainId, //TODO: return User instead of id if needed
        String name,
        String tag,
        LocalDateTime creationDate
        //TODO: return List<User> (members) if needed
) {
}
