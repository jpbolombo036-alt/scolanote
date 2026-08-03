package com.bulletin.entity;

import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "report_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportTemplate extends BaseEntity {
    @Column(nullable = false, length = 100)
    private String nom;

    @Column(length = 50)
    private String code;

    @Column(length = 500)
    private String description;

    private boolean actif;
}
