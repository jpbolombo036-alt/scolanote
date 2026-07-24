package com.bulletin.repository;

import com.bulletin.entity.Classroom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    List<Classroom> findByAcademicYearId(Long academicYearId);
    List<Classroom> findByAcademicYearIdIn(List<Long> academicYearIds);
    Page<Classroom> findByAcademicYearIdIn(List<Long> academicYearIds, Pageable pageable);
    List<Classroom> findByActiveTrue();
}
