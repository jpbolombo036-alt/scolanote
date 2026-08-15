package com.bulletin.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student extends BaseEntity {
    @Column(length = 50)
    private String matricule;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(length = 100)
    private String postnom;

    @Column(length = 100)
    private String prenom;

    @Column(length = 10)
    private String sexe;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @Column(name = "lieu_naissance", length = 150)
    private String lieuNaissance;

    @Column(length = 300)
    private String adresse;

    @Column(name = "telephone_parent", length = 50)
    private String telephoneParent;

    @Column(name = "email_parent", length = 150)
    private String emailParent;
    // Identité du parent / tuteur (utilisée pour provisionner le compte PARENT)
    @Column(name = "nom_parent", length = 100)
    private String nomParent;

    @Column(name = "postnom_parent", length = 100)
    private String postnomParent;

    @Column(name = "prenom_parent", length = 100)
    private String prenomParent;


    @Column(length = 500)
    private String photo;

    @Column(length = 30)
    private String etat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", insertable = false, updatable = false)
    private School school;

    @Column(name = "school_id")
    private Long schoolId;
}




