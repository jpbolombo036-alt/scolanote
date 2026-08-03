package com.bulletin.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Permission granulaire du système RBAC (ex: BULLETIN_GENERER, NOTE_MODIFIER).
 * Une permission est associée à un ou plusieurs rôles via {@link RolePermission}.
 * Les permissions d'un utilisateur = union des permissions de ses rôles.
 */
@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission extends BaseEntity {

    /** Code unique de la permission (ex: BULLETIN_GENERER). Utilisé dans hasPermission(). */
    @Column(unique = true, nullable = false, length = 100)
    private String code;

    /** Libellé lisible (ex: "Générer les bulletins"). */
    @Column(nullable = false, length = 200)
    private String libelle;

    /** Catégorie fonctionnelle (ex: BULLETINS, NOTES, ELEVES, ADMIN). */
    @Column(length = 50)
    private String categorie;
}
