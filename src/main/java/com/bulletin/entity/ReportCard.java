package com.bulletin.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "report_cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportCard extends BaseEntity {

    public enum Statut {
        BROUILLON,
        VALIDE_PREFET,
        VALIDE_DIRECTEUR,
        SIGNE,
        PUBLIE
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id")
    private Enrollment enrollment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "period_id")
    private Period period;

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
    private String conduite;

    @Column(length = 30)
    private String application;

    @Column(name = "date_generation")
    private LocalDateTime dateGeneration;

    @Column(name = "pdf_url", length = 500)
    private String pdfUrl;

    @Column(name = "statut", length = 30)
    @Builder.Default
    private String statut = Statut.BROUILLON.name();

    @Column(name = "valide_par_prefet_at")
    private LocalDateTime valideParPrefetAt;

    @Column(name = "valide_par_directeur_at")
    private LocalDateTime valideParDirecteurAt;

    @Column(name = "signe_at")
    private LocalDateTime signeAt;

    @Column(name = "publie_at")
    private LocalDateTime publieAt;

    @Column(name = "signature_url", length = 500)
    private String signatureUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", insertable = false, updatable = false)
    private School school;

    @Column(name = "school_id")
    private Long schoolId;

    @Override
    protected void onCreate() {
        super.onCreate();
        if (enrollment != null && enrollment.getStudent() != null && enrollment.getStudent().getSchoolId() != null && schoolId == null) {
            schoolId = enrollment.getStudent().getSchoolId();
        }
    }
}
