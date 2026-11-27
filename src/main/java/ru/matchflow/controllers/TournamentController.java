package ru.matchflow.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
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

import java.net.URI;
import java.security.Principal;

@RestController
@RequestMapping("/api/tournaments")
public class TournamentController {
    private final TournamentService tournamentService;

    public TournamentController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    @PostMapping
    public ResponseEntity<TournamentResponse> create(
            Principal principal,
            @Valid @RequestBody TournamentRequest request
    ) {
        int adminId = Integer.parseInt(principal.getName());
        TournamentResponse response = TournamentResponse.from(tournamentService.create(adminId, request));
        return ResponseEntity.created(URI.create("/api/tournaments/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    public TournamentResponse getById(@Positive @PathVariable int id) {
        return TournamentResponse.from(tournamentService.getById(id));
    }
}
