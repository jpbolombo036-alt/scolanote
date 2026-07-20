package com.bulletin.repository;

import com.bulletin.entity.AcademicYear;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AcademicYearRepository extends JpaRepository<AcademicYear, Long> {
    List<AcademicYear> findBySchoolId(Long schoolId);
    List<AcademicYear> findByActiveTrue();
}
