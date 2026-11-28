package ru.matchflow.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import ru.matchflow.entities.Application;

import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Integer> {
    @Procedure(procedureName = "register_application")
    void registerApplication(
            @Param("p_tid") Integer tournamentId,
            @Param("p_uid") Integer userId,
            @Param("p_tmid") Integer teamId
    );

    Optional<Application> findTopByTournamentIdAndParticipantIdOrderByIdDesc(Integer tournamentId, Integer userId);
    Optional<Application> findTopByTournamentIdAndTeamIdOrderByIdDesc(Integer tournamentId, Integer teamId);
}
