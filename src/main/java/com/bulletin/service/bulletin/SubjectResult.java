package com.bulletin.service.bulletin;

import com.bulletin.entity.Subject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class SubjectResult {

    private Subject subject;
    private Integer coefficient;
    private BigDecimal moyenne;
    private BigDecimal points;
    private BigDecimal maximum;
    private BigDecimal pourcentage;
    private String observation;
}
