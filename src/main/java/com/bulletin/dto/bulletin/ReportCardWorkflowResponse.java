package com.bulletin.dto.bulletin;

import com.bulletin.entity.ReportCard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportCardWorkflowResponse {

    private Long id;
    private ReportCard.Statut statut;
    private String signatureUrl;
    private java.time.LocalDateTime valideParPrefetAt;
    private java.time.LocalDateTime valideParDirecteurAt;
    private java.time.LocalDateTime signeAt;
    private java.time.LocalDateTime publieAt;
}
