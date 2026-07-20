package com.bulletin.repository;

import com.bulletin.entity.Curriculum;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CurriculumRepository extends JpaRepository<Curriculum, Long> {
    List<Curriculum> findByLevelId(Long levelId);
    List<Curriculum> findBySectionId(Long sectionId);
    List<Curriculum> findByOptionId(Long optionId);
}
