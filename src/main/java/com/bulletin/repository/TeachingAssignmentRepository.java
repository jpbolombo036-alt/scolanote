package com.bulletin.repository;

import com.bulletin.entity.TeachingAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TeachingAssignmentRepository extends JpaRepository<TeachingAssignment, Long> {
    List<TeachingAssignment> findByTeacherId(Long teacherId);
    List<TeachingAssignment> findByClassroomId(Long classroomId);
    List<TeachingAssignment> findBySubjectId(Long subjectId);
    List<TeachingAssignment> findByTeacherIdAndClassroomId(Long teacherId, Long classroomId);
}
