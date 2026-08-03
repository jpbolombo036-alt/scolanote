package com.bulletin.entity;

import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "classrooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Classroom extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id")
    private AcademicYear academicYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_id")
    private Level level;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private Section section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id")
    private Option option;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_template_id")
    private ReportTemplate reportTemplate;

    @Column(nullable = false, length = 100)
    private String nom;

    private Integer capacite;

    /**
     * Titulaire (professeur responsable) de la classe.
     * Relation vers l'entité Teacher (colonne titulaire_id).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "titulaire_id")
    private Teacher titulaire;

    private boolean active;

    /**
     * Compatibilité avec l'ancien champ Long titulaireId.
     * Permet au code existant (BulletinPdfService, ReportCardService) de continuer
     * à appeler getTitulaireId() sans modification.
     */
    public Long getTitulaireId() {
        return titulaire != null ? titulaire.getId() : null;
    }

    public void setTitulaireId(Long titulaireId) {
        if (titulaireId == null) {
            this.titulaire = null;
        } else {
            Teacher t = new Teacher();
            t.setId(titulaireId);
            this.titulaire = t;
        }
    }
}
