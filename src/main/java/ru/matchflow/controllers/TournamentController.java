package ru.matchflow.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.matchflow.dto.TournamentRequest;
import ru.matchflow.dto.TournamentResponse;
import ru.matchflow.services.TournamentService;

@RestController
@RequestMapping("/api/tournaments")
@RequiredArgsConstructor
@Validated
public class TournamentController {

    private final TournamentService tournamentService;

    @PostMapping
    public ResponseEntity<TournamentResponse> createTournament(
            @NotNull
            @Positive
            @RequestParam
            Integer userId,
            @Valid
            @RequestBody
            TournamentRequest request
    ) {
        TournamentResponse response = tournamentService.createTournament(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TournamentResponse> getById(
            @Positive
            @PathVariable
            Integer id) {
        return ResponseEntity.ok(tournamentService.getTournamentById(id));
    }

    @PutMapping("/{tournamentId}")
    public ResponseEntity<TournamentResponse> updateTournament(
            @NotNull
            @Positive
            @RequestParam
            Integer userId,
            @Positive
            @PathVariable
            Integer tournamentId,
            @RequestBody TournamentRequest request
    ) {
        return ResponseEntity.ok(tournamentService.updateTournament(userId, tournamentId, request));
    }

    @DeleteMapping("/{tournamentId}")
    public ResponseEntity<Void> deleteTournament(
            @NotNull
            @Positive
            @RequestParam
            Integer userId,
            @Positive
            @PathVariable
            Integer tournamentId
    ) {
        tournamentService.deleteTournament(userId, tournamentId);
        return ResponseEntity.noContent().build();
    }
}
