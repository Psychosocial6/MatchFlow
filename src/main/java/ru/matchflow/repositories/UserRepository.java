package ru.matchflow.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.matchflow.entities.UserAccount;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserAccount, Integer> {
    Optional<UserAccount> findByUsername(String username);
}
