package com.bulletin.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "periods")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@org.hibernate.annotations.Where(clause = "deleted_at IS NULL")
public class Period {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trimester_id")
    private Trimester trimester;

    @Column(nullable = false, length = 100)
    private String nom;

    private Integer ordre;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PeriodType type;

    @Column(name = "date_debut")
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column(name = "verrouille")
    @Builder.Default
    private boolean verrouille = false;

    @Column(name = "date_verrouillage")
    private LocalDateTime dateVerrouillage;

    @Column(name = "verrouille_par")
    private String verrouillePar;

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
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum PeriodType {
        PERIODE, EXAMEN
    }
}
