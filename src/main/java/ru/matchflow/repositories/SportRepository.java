package ru.matchflow.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.matchflow.entities.Sport;

@Repository
public interface SportRepository extends JpaRepository<Sport, Integer> {
}
