package com.bulletin.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "report_card_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportCardDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_card_id")
    private ReportCard reportCard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    private Integer coefficient;

    @Column(precision = 8, scale = 2)
    private BigDecimal moyenne;

    @Column(precision = 10, scale = 2)
    private BigDecimal points;

    @Column(precision = 10, scale = 2)
    private BigDecimal maximum;

    @Column(precision = 8, scale = 2)
    private BigDecimal pourcentage;

    @Column(length = 500)
    private String observation;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
