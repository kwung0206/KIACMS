package com.kiacms.course.entity;

import com.kiacms.course.enums.SessionWatchState;
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
        name = "session_watch_statuses",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_watch_status_student_session",
                        columnNames = {"student_id", "course_session_id"}
                )
        },
        indexes = {
                @Index(name = "idx_watch_status_student", columnList = "student_id, status"),
                @Index(name = "idx_watch_status_session", columnList = "course_session_id, status")
        }
)
public class SessionWatchStatus extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_session_id", nullable = false)
    private CourseSession courseSession;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private SessionWatchState status = SessionWatchState.NOT_STARTED;

    @Column(name = "last_watched_at")
    private Instant lastWatchedAt;

    @Column(name = "completed_at")
    private Instant completedAt;
}
