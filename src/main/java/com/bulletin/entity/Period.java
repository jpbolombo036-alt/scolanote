package com.bulletin.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "periods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Period extends BaseEntity {
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", insertable = false, updatable = false)
    private School school;

    @Column(name = "school_id")
    private Long schoolId;
    public enum PeriodType {
        PERIODE, EXAMEN
    }
}




