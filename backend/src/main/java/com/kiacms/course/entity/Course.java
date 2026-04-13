package com.kiacms.course.entity;

import com.kiacms.course.enums.CourseStatus;
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
import java.time.LocalDate;
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
        name = "courses",
        indexes = {
                @Index(name = "idx_courses_status", columnList = "status"),
                @Index(name = "idx_courses_creator", columnList = "created_by_id")
        }
)
public class Course extends BaseEntity {

    @Column(name = "course_code", nullable = false, unique = true, length = 50)
    private String courseCode;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "track_name", length = 100)
    private String trackName;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private CourseStatus status = CourseStatus.PLANNED;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "max_capacity")
    private Integer maxCapacity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;
}
