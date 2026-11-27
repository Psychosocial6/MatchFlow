package ru.matchflow.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.matchflow.dto.TournamentRequest;
import ru.matchflow.dto.TournamentResponse;
import ru.matchflow.services.TournamentService;
import java.security.Principal;

@RestController
@RequestMapping("/api/tournaments")
@RequiredArgsConstructor
public class TournamentController {

    private final TournamentService tournamentService;

    @PostMapping
    public ResponseEntity<TournamentResponse> createTournament(
            Principal principal,
            @Valid @RequestBody TournamentRequest request
    ) {
        int adminId = Integer.parseInt(principal.getName());
        TournamentResponse response = tournamentService.createTournament(request, adminId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TournamentResponse> getById(@Positive @PathVariable int id) {
        return ResponseEntity.ok(tournamentService.getTournamentById(id));
    }
}
