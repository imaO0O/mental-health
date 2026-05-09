package ru.rrtu.mental_health_system.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Журнал аудита изменений в БД.
 *
 * Соответствует таблице «audit_log» из ПЗ. Записи добавляются
 * автоматически процедурой sp_audit_log при операциях INSERT/UPDATE/DELETE
 * над справочниками и тестами, обеспечивая прозрачность действий
 * администратора и психолога.
 */
@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_number")
    private Long auditNumber;

    @Column(name = "action_time", nullable = false)
    private LocalDateTime actionTime;

    /** Логин инициатора операции (FK к users по login). */
    @Column(name = "login", length = 50)
    private String login;

    /** INSERT / UPDATE / DELETE */
    @Column(name = "operation", length = 20, nullable = false)
    private String operation;

    @Column(name = "table_name", length = 40, nullable = false)
    private String tableName;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @PrePersist
    protected void onCreate() {
        if (actionTime == null) actionTime = LocalDateTime.now();
    }

    public Long getAuditNumber() { return auditNumber; }
    public void setAuditNumber(Long auditNumber) { this.auditNumber = auditNumber; }

    public LocalDateTime getActionTime() { return actionTime; }
    public void setActionTime(LocalDateTime actionTime) { this.actionTime = actionTime; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
