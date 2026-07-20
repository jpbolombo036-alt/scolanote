package com.bulletin.repository;

import com.bulletin.entity.ReportCardDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReportCardDetailRepository extends JpaRepository<ReportCardDetail, Long> {
    List<ReportCardDetail> findByReportCardId(Long reportCardId);
}
