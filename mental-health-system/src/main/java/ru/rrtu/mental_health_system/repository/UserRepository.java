package ru.rrtu.mental_health_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rrtu.mental_health_system.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    default Optional<User> findByLogin(String login) { return findById(login); }
    default boolean existsByLogin(String login) { return existsById(login); }
}
