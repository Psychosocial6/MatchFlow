package ru.matchflow.controllers;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.matchflow.dto.ApplicationRequest;
import ru.matchflow.dto.ApplicationResponse;
import ru.matchflow.services.ApplicationService;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    public final ApplicationService applicationService;

    //TODO: get applications with sorting, filtering and paging\

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getApplicationById(
            @Positive
            @PathVariable
            Integer id
    ) {
        ApplicationResponse response = applicationService.getApplicationById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> createApplication(
            @Valid
            @RequestBody
            ApplicationRequest request
    ) {
        ApplicationResponse response = applicationService.createApplication(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> approveApplication(
            @PathVariable
            @Positive
            Integer id
    ) {
        applicationService.approveApplication(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Void> rejectApplication(
            @PathVariable
            @Positive
            Integer id
    ) {
        applicationService.rejectApplication(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(
            @PathVariable
            @Positive
            Integer id,
            @RequestParam
            @Positive
            Integer userId
    ) {
        applicationService.deleteApplication(id, userId);
        return ResponseEntity.ok().build();
    }
}
