package com.bulletin.repository;

import com.bulletin.entity.UserStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserStudentRepository extends JpaRepository<UserStudent, Long> {
    List<UserStudent> findBySchoolId(Long schoolId);

    /** Indique si un lien user-élève existe déjà (anti-doublon). */
    boolean existsByUser_IdAndStudent_Id(Long userId, Long studentId);

    List<UserStudent> findByUserId(Long userId);
    List<UserStudent> findByStudentId(Long studentId);
}
