package ru.rrtu.mental_health_system.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Пользователь системы (учётная запись).
 *
 * Согласно проектировке (см. ПЗ, таблица «users»),
 * первичный ключ — естественный: логин (login).
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "login", length = 50, nullable = false)
    private String login;

    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_name", nullable = false)
    private Role role;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /**
     * Алиас «id» для обратной совместимости с теми местами кода,
     * где раньше использовался суррогатный user_id.
     * Возвращает логин, который теперь является PK.
     */
    public String getId() { return login; }
}
