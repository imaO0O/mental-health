package ru.rrtu.mental_health_system.model;

import jakarta.persistence.*;

/**
 * Студент.
 *
 * Согласно проектировке (см. ПЗ, таблица «students»),
 * первичный ключ — естественный: номер зачётной книжки.
 * Внешний ключ — логин учётной записи (User.login).
 */
@Entity
@Table(name = "students")
public class Student {

    @Id
    @Column(name = "record_book_number", nullable = false)
    private Long recordBookNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "login", nullable = false, unique = true)
    private User user;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "middle_name", length = 50)
    private String middleName;

    @Column(name = "group_name", nullable = false, length = 20)
    private String groupName;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    public Long getRecordBookNumber() { return recordBookNumber; }
    public void setRecordBookNumber(Long n) { this.recordBookNumber = n; }

    /** Алиас id == recordBookNumber. */
    public Long getId() { return recordBookNumber; }
    public void setId(Long id) { this.recordBookNumber = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getLastName() { return lastName; }
    public void setLastName(String s) { this.lastName = s; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String s) { this.firstName = s; }

    public String getMiddleName() { return middleName; }
    public void setMiddleName(String s) { this.middleName = s; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String s) { this.groupName = s; }

    public String getEmail() { return email; }
    public void setEmail(String s) { this.email = s; }

    public String getPhone() { return phone; }
    public void setPhone(String s) { this.phone = s; }

    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        sb.append(lastName);
        if (firstName != null && !firstName.isEmpty()) sb.append(" ").append(firstName);
        if (middleName != null && !middleName.isEmpty()) sb.append(" ").append(middleName);
        return sb.toString().trim();
    }

    public String getUsername() { return user != null ? user.getLogin() : null; }
}
