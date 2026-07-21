package com.bulletin.repository;

import com.bulletin.entity.Trimester;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TrimesterRepository extends JpaRepository<Trimester, Long> {
    List<Trimester> findByAcademicYearId(Long academicYearId);
}
