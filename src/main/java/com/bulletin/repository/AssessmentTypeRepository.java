package com.bulletin.repository;

import com.bulletin.entity.AssessmentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AssessmentTypeRepository extends JpaRepository<AssessmentType, Long> {
    List<AssessmentType> findBySchoolId(Long schoolId);
    Page<AssessmentType> findBySchoolId(Long schoolId, Pageable pageable);
    long countBySchoolId(Long schoolId);
}
