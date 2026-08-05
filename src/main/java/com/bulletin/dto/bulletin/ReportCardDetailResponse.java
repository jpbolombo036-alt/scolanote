package com.bulletin.dto.bulletin;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    /**
     * L'entité métier expose cette donnée sous le nom {@code observation} (voir
     * ReportCardDetail.observation). Le frontend consomme la clé JSON {@code observation}.
     */
    @JsonProperty("observation")
    private String appreciation;
}
