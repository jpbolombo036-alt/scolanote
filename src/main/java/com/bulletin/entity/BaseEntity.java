package com.bulletin.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

/**
 * Classe de base pour toutes les entités métier.
 *
 * Centralise :
 *  - la clé primaire {@code id}
 *  - les timestamps {@code created_at} / {@code updated_at} (gérés automatiquement)
 *  - le soft-delete {@code deleted_at} (+ filtre global {@link SQLRestriction})
 *
 * Le filtre {@code deleted_at IS NULL} s'applique à toutes les requêtes sur les entités
 * héritant de cette classe : les enregistrements "supprimés" (soft-delete) sont invisibles.
 *
 * Note sur le multi-tenant : la colonne {@code school_id} reste gérée par chaque entité
 * concrète (pattern School + schoolId) car elle n'est pas pertinente pour toutes
 * (ex: School, Role) et son renseignement varie selon l'entité.
 */
@MappedSuperclass
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    /** Marque l'entité comme supprimée (soft-delete). */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
