package ru.matchflow.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.matchflow.entities.Tournament;

public interface TournamentRepository extends JpaRepository<Tournament, Integer> {
    @Modifying
    @Query(value = """
    CALL set_tournament_status(
        :id,
        CAST(:status AS enum_status_tournaments)
    )
    """, nativeQuery = true)
    void setTournamentStatus(
            @Param("id") Integer id,
            @Param("status") String status
    );
}
