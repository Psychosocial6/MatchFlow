package ru.matchflow.repositories;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.matchflow.entities.UserAccount;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserAccount, Integer> {
    Optional<UserAccount> findByUsername(String username);

    Optional<UserAccount> findByEmail(String email);

    Optional<UserAccount> findByVkId(String vkId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT account FROM UserAccount account WHERE account.id = :id")
    Optional<UserAccount> findByIdForUpdate(@Param("id") int id);
}
