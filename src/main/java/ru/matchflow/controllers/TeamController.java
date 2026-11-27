package ru.matchflow.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.matchflow.dto.TeamRequest;
import ru.matchflow.dto.TeamResponse;
import ru.matchflow.services.TeamService;

import java.security.Principal;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    public ResponseEntity<TeamResponse> createTeam(Principal principal, @Valid @RequestBody TeamRequest request) {
        int captainId = Integer.parseInt(principal.getName());
        TeamResponse response = teamService.createTeam(request, captainId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
