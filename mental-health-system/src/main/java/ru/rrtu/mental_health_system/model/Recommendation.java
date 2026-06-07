package ru.rrtu.mental_health_system.model;

import jakarta.persistence.*;

/**
 * Рекомендация для определённой градации показателя (таблица «recommendations»).
 * Первичный ключ — «Код рекомендации». Внешние ключи: градация и автор-психолог.
 */
@Entity
@Table(name = "recommendations")
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommendation_code")
    private Long recommendationCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_name", nullable = false)
    private Grade grade;

    @Column(name = "recommendation_text", nullable = false, columnDefinition = "TEXT")
    private String recommendationText;

    @Column(name = "order_number")
    private Short orderNumber;

    /** Психолог-автор рекомендации (FK на personnel_number). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_personnel_number")
    private Psychologist author;

    public Long getRecommendationCode() { return recommendationCode; }
    public void setRecommendationCode(Long c) { this.recommendationCode = c; }

    public Long getId() { return recommendationCode; }
    public void setId(Long id) { this.recommendationCode = id; }

    public Grade getGrade() { return grade; }
    public void setGrade(Grade g) { this.grade = g; }

    /** Алиасы для совместимости (stressLevel). */
    public Grade getStressLevel() { return grade; }
    public void setStressLevel(Grade g) { this.grade = g; }

    public String getRecommendationText() { return recommendationText; }
    public void setRecommendationText(String s) { this.recommendationText = s; }

    public Short getOrderNumber() { return orderNumber; }
    public void setOrderNumber(Short n) { this.orderNumber = n; }

    public Psychologist getAuthor() { return author; }
    public void setAuthor(Psychologist a) { this.author = a; }
}
