package com.bulletin.dto.bulletin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcademicYearReportCardResponse {

    private Long id;
    private Long enrollmentId;
    private Long studentId;
    private String studentNom;
    private String studentMatricule;
    private String eleveNomComplet;
    private String elevePrenom;
    private String eleveNom;
    private Long classroomId;
    private String classroomNom;
    private Long academicYearId;
    private String academicYearNom;
    private BigDecimal pourcentage;
    private BigDecimal moyenne;
    private BigDecimal totalPoints;
    private BigDecimal maximumPoints;
    private Integer rang;
    private String mention;
    private String decision;
    private LocalDateTime dateGeneration;
    private String pdfUrl;
    private String statut;
    private List<AcademicYearReportCardDetailResponse> details;
    private Integer totalAbsences;
    private Integer totalRetards;
    private String conduite;
    private String application;
}
