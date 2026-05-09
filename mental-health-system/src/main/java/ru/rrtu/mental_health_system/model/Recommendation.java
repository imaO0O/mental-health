package ru.rrtu.mental_health_system.model;

import jakarta.persistence.*;

/**
 * Рекомендация для определённого уровня стресса.
 *
 * Согласно проектировке (см. ПЗ, таблица «recommendations»),
 * первичный ключ — естественный «Код рекомендации».
 * Дополнительный внешний ключ — автор-психолог (author_personnel_number).
 */
@Entity
@Table(name = "recommendations")
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommendation_code")
    private Long recommendationCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_name", nullable = false)
    private StressLevel stressLevel;

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

    /** Алиас id == recommendationCode. */
    public Long getId() { return recommendationCode; }
    public void setId(Long id) { this.recommendationCode = id; }

    public StressLevel getStressLevel() { return stressLevel; }
    public void setStressLevel(StressLevel sl) { this.stressLevel = sl; }

    public String getRecommendationText() { return recommendationText; }
    public void setRecommendationText(String s) { this.recommendationText = s; }

    public Short getOrderNumber() { return orderNumber; }
    public void setOrderNumber(Short n) { this.orderNumber = n; }

    public Psychologist getAuthor() { return author; }
    public void setAuthor(Psychologist a) { this.author = a; }
}
