package com.bulletin.repository;

import com.bulletin.entity.Trimester;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TrimesterRepository extends JpaRepository<Trimester, Long> {
    List<Trimester> findByAcademicYearId(Long academicYearId);
    List<Trimester> findByAcademicYearIdIn(List<Long> academicYearIds);
    Page<Trimester> findByAcademicYearIdIn(List<Long> academicYearIds, Pageable pageable);
}
