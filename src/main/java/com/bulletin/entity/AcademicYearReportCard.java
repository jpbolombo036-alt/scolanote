package com.bulletin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "academic_year_report_cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicYearReportCard extends BaseEntity {

    public enum Statut {
        BROUILLON,
        VALIDE_PREFET,
        VALIDE_DIRECTEUR,
        SIGNE,
        PUBLIE
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    @Column(name = "pourcentage", precision = 5, scale = 2)
    private BigDecimal pourcentage;

    @Column(name = "total_points", precision = 10, scale = 2)
    private BigDecimal totalPoints;

    @Column(name = "maximum_points", precision = 10, scale = 2)
    private BigDecimal maximumPoints;

    private Integer rang;

    @Column(length = 50)
    private String mention;

    @Column(length = 50)
    private String decision;

    @Column(name = "total_absences")
    private Integer totalAbsences;

    @Column(name = "total_retards")
    private Integer totalRetards;

    @Column(length = 30)
    private String conduite; // Agrégation de la conduite sur l'année

    @Column(length = 30)
    private String application; // Agrégation de l'application sur l'année

    @Column(name = "date_generation")
    private LocalDateTime dateGeneration;

    @Column(name = "pdf_url", length = 500)
    private String pdfUrl;

    @Column(name = "statut", length = 30)
    @Builder.Default
    private String statut = Statut.BROUILLON.name();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", insertable = false, updatable = false)
    private School school;

    @Column(name = "school_id")
    private Long schoolId;

    // Champs d'audit (qui a validé/signé) - à ajouter si nécessaire, comme pour ReportCard
    // private User valideParPrefetBy;
    // private LocalDateTime valideParPrefetAt;
    // ...

    @OneToMany(mappedBy = "academicYearReportCard", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<AcademicYearReportCardDetail> details;

    @Override
    protected void onCreate() {
        super.onCreate();
        if (enrollment != null && enrollment.getStudent() != null && enrollment.getStudent().getSchoolId() != null && schoolId == null) {
            schoolId = enrollment.getStudent().getSchoolId();
        }
    }
}