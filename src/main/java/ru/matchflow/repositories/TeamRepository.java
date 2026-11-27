package ru.matchflow.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.matchflow.entities.Team;

public interface TeamRepository extends JpaRepository<Team, Integer> {
}
