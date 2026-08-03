package com.bulletin.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
@Entity
@Table(name = "report_card_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportCardDetail extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_card_id")
    private ReportCard reportCard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    private Integer coefficient;

    @Column(precision = 8, scale = 2)
    private BigDecimal moyenne;

    @Column(precision = 10, scale = 2)
    private BigDecimal points;

    @Column(precision = 10, scale = 2)
    private BigDecimal maximum;

    @Column(precision = 8, scale = 2)
    private BigDecimal pourcentage;

    private Integer rangMatiere;

    @Column(length = 500)
    private String observation;
}
