package ru.rrtu.mental_health_system.model;

import jakarta.persistence.*;

/**
 * Роль пользователя.
 *
 * Согласно проектировке (см. ПЗ, таблица «roles»),
 * первичный ключ — естественный: название роли (STUDENT, PSYCHOLOGIST, ADMIN).
 */
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @Column(name = "role_name", length = 30, nullable = false)
    private String roleName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    public Role() {}

    public Role(String roleName) {
        this.roleName = roleName;
    }

    public Role(String roleName, String description) {
        this.roleName = roleName;
        this.description = description;
    }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    /** Алиасы для обратной совместимости. */
    public String getName() { return roleName; }
    public void setName(String n) { this.roleName = n; }
    public String getId() { return roleName; }
    public void setId(String id) { this.roleName = id; }
}
