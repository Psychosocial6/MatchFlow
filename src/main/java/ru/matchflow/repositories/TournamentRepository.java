package ru.matchflow.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.matchflow.entities.Tournament;

public interface TournamentRepository extends JpaRepository<Tournament, Integer> {
}
