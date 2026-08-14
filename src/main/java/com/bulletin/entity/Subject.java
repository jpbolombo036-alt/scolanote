package com.bulletin.entity;

import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "subjects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subject extends BaseEntity {
    @Column(length = 20)
    private String code;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(length = 500)
    private String description;

    private Integer coefficient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", insertable = false, updatable = false)
    private School school;

    @Column(name = "school_id")
    private Long schoolId;
}




