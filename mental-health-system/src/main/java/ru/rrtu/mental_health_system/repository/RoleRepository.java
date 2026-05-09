package ru.rrtu.mental_health_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rrtu.mental_health_system.model.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, String> {
    /** Согласно ПЗ, PK у roles — естественный role_name, поэтому findById ≡ findByName. */
    default Optional<Role> findByName(String name) {
        return findById(name);
    }
}
