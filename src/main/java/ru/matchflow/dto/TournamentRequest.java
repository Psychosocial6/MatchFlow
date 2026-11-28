package ru.matchflow.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.CodePointLength;
import ru.matchflow.entities.Tournament;

import java.time.LocalDateTime;

public record TournamentRequest(
        @NotNull
        @Positive
        Integer sportId,
        @CodePointLength(max = 255)
        String title,
        String regulations,
        @NotNull
        Tournament.ParticipationType participationType,
        @NotNull
        Tournament.Format format,
        @NotNull
        LocalDateTime regStartDate,
        @NotNull
        LocalDateTime regEndDate,
        @NotNull
        LocalDateTime startDate,
        @NotNull
        LocalDateTime endDate,
        @NotNull
        @Min(2)
        Integer maxParticipants
) {
}
