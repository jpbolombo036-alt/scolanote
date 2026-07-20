package com.bulletin.repository;

import com.bulletin.entity.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {
    List<Assessment> findByAssignmentId(Long assignmentId);
    List<Assessment> findByTermId(Long termId);
}
