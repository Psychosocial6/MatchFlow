package ru.matchflow.utils;

import org.springframework.stereotype.Component;
import ru.matchflow.dto.ApplicationResponse;
import ru.matchflow.entities.Application;

@Component
public class ApplicationMapper {
    public ApplicationResponse fromEntity(Application application) {
        return new ApplicationResponse(
                application.getId(),
                application.getTournament().getId(),
                application.getParticipant().getId(),
                application.getTeam().getId(),
                application.getStatus(),
                application.getCreationDate()
        );
    }
}
