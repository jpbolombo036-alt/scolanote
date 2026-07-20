package com.bulletin.dto.bulletin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.bulletin.dto.bulletin.ReportCardDetailResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportCardResponse {

    private Long id;
    private Long enrollmentId;
    private Long studentId;
    private String studentNom;
    private String studentMatricule;
    private Long classroomId;
    private String classroomNom;
    private Long termId;
    private String termNom;
    private BigDecimal pourcentage;
    private BigDecimal totalPoints;
    private BigDecimal maximumPoints;
    private Integer rang;
    private String mention;
    private String decision;
    private LocalDateTime dateGeneration;
    private String pdfUrl;
    private List<ReportCardDetailResponse> details;
}
