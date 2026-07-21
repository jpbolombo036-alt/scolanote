package com.bulletin.dto.bulletin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportCardDetailResponse {

    private Long id;
    private Long subjectId;
    private String subjectNom;
    private String subjectCode;
    private Integer coefficient;
    private BigDecimal moyenne;
    private Integer rangMatiere;
    private BigDecimal points;
    private BigDecimal maximum;
    private BigDecimal pourcentage;
    private String appreciation;
}
