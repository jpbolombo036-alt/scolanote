package com.bulletin.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "report_cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@org.hibernate.annotations.Where(clause = "deleted_at IS NULL")
public class ReportCard {

    public enum Statut {
        BROUILLON,
        VALIDE_PREFET,
        VALIDE_DIRECTEUR,
        SIGNE,
        PUBLIE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id")
    private Enrollment enrollment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "term_id")
    private Term term;

    @Column(precision = 8, scale = 2)
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

    @Column(name = "date_generation")
    private LocalDateTime dateGeneration;

    @Column(name = "pdf_url", length = 500)
    private String pdfUrl;

    @Column(name = "statut", length = 30)
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

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (dateGeneration == null) {
            dateGeneration = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
