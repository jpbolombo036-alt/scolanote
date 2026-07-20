package com.bulletin.repository;

import com.bulletin.entity.CurriculumSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CurriculumSubjectRepository extends JpaRepository<CurriculumSubject, Long> {
    List<CurriculumSubject> findByCurriculumId(Long curriculumId);
    List<CurriculumSubject> findBySubjectId(Long subjectId);
}
