package com.kiacms.course.entity;

import com.kiacms.course.enums.EnrollmentStatus;
import com.kiacms.global.entity.BaseEntity;
import com.kiacms.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "enrollments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_enrollment_student_course", columnNames = {"student_id", "course_id"})
        },
        indexes = {
                @Index(name = "idx_enrollments_course_status", columnList = "course_id, status"),
                @Index(name = "idx_enrollments_student_status", columnList = "student_id, status")
        }
)
public class Enrollment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private EnrollmentStatus status = EnrollmentStatus.ENROLLED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrolled_by_id")
    private User enrolledBy;

    @Column(name = "completed_at")
    private Instant completedAt;
}
