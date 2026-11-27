package ru.matchflow.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.matchflow.entities.Tournament;
import ru.matchflow.entities.UserAccount;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Component
public class TournamentManagementValidator {

    public void validate(UserAccount user, Tournament tournament) {
        List<String> errors = new ArrayList<>();
        if (tournament.getStatus() != Tournament.Status.DRAFT) {
            errors.add("Tournament can be updated or deleted only in DRAFT status");
        }
        if (!Objects.equals(tournament.getAdmin().getId(), user.getId())) {
            errors.add("Ony admin can update or delete tournament");
        }
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Validation error: " + String.join("\n", errors));
        }
    }
}
