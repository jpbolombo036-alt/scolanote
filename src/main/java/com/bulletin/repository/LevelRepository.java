package com.bulletin.repository;

import com.bulletin.entity.Level;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface LevelRepository extends JpaRepository<Level, Long> {
    List<Level> findBySchoolId(Long schoolId);
    Page<Level> findBySchoolId(Long schoolId, Pageable pageable);
    long countBySchoolId(Long schoolId);

    /**
     * Nombre de niveaux partageant le même ordre dans une école (utilisé pour
     * vérifier l'unicité côté application).
     */
    long countBySchoolIdAndOrdre(Long schoolId, Integer ordre);

    /**
     * Ordre maximum existant dans une école (NULL si aucun). MAX ignore les valeurs NULL.
     */
    @Query("SELECT MAX(l.ordre) FROM Level l WHERE l.schoolId = :schoolId")
    Integer maxOrdreBySchoolId(@Param("schoolId") Long schoolId);
}
