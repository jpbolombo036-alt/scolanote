package com.bulletin.repository;

import com.bulletin.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GradeRepository extends JpaRepository<Grade, Long> {
    List<Grade> findByAssessmentId(Long assessmentId);
    List<Grade> findByStudentId(Long studentId);
}
