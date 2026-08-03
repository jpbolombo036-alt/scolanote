package com.bulletin.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "enrollments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id")
    private Classroom classroom;

    @Column(name = "date_inscription")
    private LocalDate dateInscription;

    @Column(name = "numero_ordre")
    private Integer numeroOrdre;

    @Column(length = 30)
    private String etat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", insertable = false, updatable = false)
    private School school;

    @Column(name = "school_id")
    private Long schoolId;

    @Override
    protected void onCreate() {
        super.onCreate();
        if (student != null && student.getSchoolId() != null && schoolId == null) {
            schoolId = student.getSchoolId();
        }
    }
}
