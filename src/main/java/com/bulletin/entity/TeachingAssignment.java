package com.bulletin.entity;

import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "teaching_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeachingAssignment extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id")
    private Classroom classroom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", insertable = false, updatable = false)
    private School school;

    @Column(name = "school_id")
    private Long schoolId;

    @Override
    protected void onCreate() {
        super.onCreate();
        if (teacher != null && teacher.getSchoolId() != null && schoolId == null) {
            schoolId = teacher.getSchoolId();
        }
    }
}
