package com.bulletin.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "schools")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class School extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String nom;

    @Column(length = 50)
    private String code;

    @Column(length = 100)
    private String province;

    @Column(name = "commune_territoire", length = 100)
    private String communeTerritoire;

    @Column(length = 300)
    private String adresse;

    @Column(length = 50)
    private String telephone;

    @Column(length = 150)
    private String email;

    @Column(length = 500)
    private String logo;
}
