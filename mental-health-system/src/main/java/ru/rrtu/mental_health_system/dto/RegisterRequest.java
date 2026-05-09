package ru.rrtu.mental_health_system.dto;

public class RegisterRequest {
    private String login;
    private String password;
    private String role;
    private String lastName;
    private String firstName;
    private String middleName;
    private String groupName;
    private String position;
    private Long recordBookNumber;
    private Long personnelNumber;
    private String email;
    private String phone;

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public Long getRecordBookNumber() { return recordBookNumber; }
    public void setRecordBookNumber(Long recordBookNumber) { this.recordBookNumber = recordBookNumber; }
    public Long getPersonnelNumber() { return personnelNumber; }
    public void setPersonnelNumber(Long personnelNumber) { this.personnelNumber = personnelNumber; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
