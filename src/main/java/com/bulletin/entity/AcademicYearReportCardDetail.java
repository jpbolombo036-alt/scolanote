package com.bulletin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "academic_year_report_card_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicYearReportCardDetail extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_report_card_id", nullable = false)
    private AcademicYearReportCard academicYearReportCard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    private Integer coefficient;

    @Column(precision = 5, scale = 2)
    private BigDecimal moyenne; // Moyenne annuelle pour la matière

    @Column(precision = 10, scale = 2)
    private BigDecimal points;

    @Column(precision = 10, scale = 2)
    private BigDecimal maximum;

    @Column(precision = 5, scale = 2)
    private BigDecimal pourcentage;

    @Column(name = "rang_matiere")
    private Integer rangMatiere; // Rang annuel pour la matière

    @Column(length = 255)
    private String observation; // Observation agrégée ou finale

    @Column(name = "moyenne_t1", precision = 5, scale = 2)
    private BigDecimal moyenneT1;

    @Column(name = "moyenne_t2", precision = 5, scale = 2)
    private BigDecimal moyenneT2;

    @Column(name = "moyenne_t3", precision = 5, scale = 2)
    private BigDecimal moyenneT3;

    @Column(name = "moyenne_examen", precision = 5, scale = 2)
    private BigDecimal moyenneExamen;
}