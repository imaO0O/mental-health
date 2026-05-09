package ru.rrtu.mental_health_system.model;

import jakarta.persistence.*;

/**
 * Психолог.
 *
 * Согласно проектировке (см. ПЗ, таблица «psychologists»),
 * первичный ключ — естественный: табельный номер.
 * Внешний ключ — логин учётной записи (User.login).
 */
@Entity
@Table(name = "psychologists")
public class Psychologist {

    @Id
    @Column(name = "personnel_number", nullable = false)
    private Long personnelNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "login", nullable = false, unique = true)
    private User user;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "middle_name", length = 50)
    private String middleName;

    @Column(name = "position", length = 100)
    private String position;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    public Long getPersonnelNumber() { return personnelNumber; }
    public void setPersonnelNumber(Long n) { this.personnelNumber = n; }

    /** Алиас id == personnelNumber. */
    public Long getId() { return personnelNumber; }
    public void setId(Long id) { this.personnelNumber = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getLastName() { return lastName; }
    public void setLastName(String s) { this.lastName = s; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String s) { this.firstName = s; }

    public String getMiddleName() { return middleName; }
    public void setMiddleName(String s) { this.middleName = s; }

    public String getPosition() { return position; }
    public void setPosition(String s) { this.position = s; }

    public String getEmail() { return email; }
    public void setEmail(String s) { this.email = s; }

    public String getPhone() { return phone; }
    public void setPhone(String s) { this.phone = s; }

    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        if (lastName != null) sb.append(lastName);
        if (firstName != null && !firstName.isEmpty()) sb.append(" ").append(firstName);
        if (middleName != null && !middleName.isEmpty()) sb.append(" ").append(middleName);
        return sb.toString().trim();
    }
}
